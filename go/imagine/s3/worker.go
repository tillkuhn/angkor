package s3

import (
	"fmt"
	"mime"
	"net/http"
	"net/url"
	"path/filepath"
	"regexp"
	"runtime/debug"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/tillkuhn/angkor/tools/imagine/image"
	"github.com/tillkuhn/angkor/tools/imagine/types"
	"github.com/tillkuhn/angkor/tools/imagine/utils"

	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/rs/xid"

	"github.com/rs/zerolog/log"

	"github.com/tillkuhn/angkor/tools/imagine/audio"

	"github.com/rs/zerolog"

	"github.com/tillkuhn/angkor/go/topkapi"

	"os"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
)

const TagContentType = "ContentType"

type Handler struct {
	session     *session.Session
	publisher   *topkapi.Client
	config      *types.Config
	uploadQueue chan types.UploadRequest
	log         zerolog.Logger
}

func NewHandler(session *session.Session, publisher *topkapi.Client, config *types.Config) *Handler {
	return &Handler{
		session:     session,
		publisher:   publisher,
		config:      config,
		uploadQueue: make(chan types.UploadRequest, config.QueueSize),
		log:         log.Logger.With().Str("logger", "s3worker").Logger(),
	}
}

// StartWorker invokes as goroutine to listen for new upload requests
func (h *Handler) StartWorker() {
	for job := range h.uploadQueue {
		h.log.Printf("Process uploadJob %v", job)
		err := h.PutObject(&job)
		if err != nil {
			h.log.Error().Msgf("PutObject - filename: %v, err: %v", job.LocalPath, err)
		}
		h.log.Printf("PutObject id=%s - success", job.RequestId)
	}
}

// UploadRequest adds a request to the queue
func (h *Handler) UploadRequest(uploadReq *types.UploadRequest) {
	h.uploadQueue <- *uploadReq
}

// PutObject Puts a new object into s3 bucket, inspired by
// - https://golangcode.com/uploading-a-file-to-s3/
// - https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/go/example_code/s3
func (h *Handler) PutObject(uploadRequest *types.UploadRequest) error {
	fileHandle, err := os.Open(uploadRequest.LocalPath)
	if err != nil {
		h.log.Err(err).Msgf("Cannot open %s: %s", uploadRequest.LocalPath, err.Error())
		return err
	}
	// Only the first 512 bytes are used to sniff the content type.
	buffer := make([]byte, 512)
	if _, err := fileHandle.Read(buffer); err != nil {
		return err
	}
	contentType := http.DetectContentType(buffer) // make a guess, or return application/octet-stream
	// fileHandle.Seek(0, io.SeekStart) // rewind my selector
	defer utils.CheckedClose(fileHandle)
	// init s3 tags, for jpeg content type parse exif and store in s3 tags
	tagMap := make(map[string]string)
	// text/xml; charset=utf-8 does not work apparently the part after ; causes trouble, so we strip it
	contentTypeForEncoding := contentType
	if strings.Contains(contentTypeForEncoding, ";") {
		contentTypeForEncoding = strings.Split(contentType, ";")[0]
	}
	h.log.Printf("contentType %s", contentTypeForEncoding) //  text/xml; charset=utf-8 does not work

	tagMap[TagContentType] = contentTypeForEncoding // contentType
	tagMap["Size"] = strconv.FormatInt(uploadRequest.Size, 10)
	tagMap["Origin"] = utils.StripRequestParams(uploadRequest.Origin) // even if encoded, ?bla=bla parts raise exceptions
	// EXIF tags can be only extracted for image/jpeg files
	if utils.IsJPEG(contentType) {
		exif, _ := image.ExtractExif(uploadRequest.LocalPath)
		// merge extracted exif tags into master tagMap
		if len(exif) > 0 {
			for key, element := range exif {
				tagMap[key] = element
			}
		}
		// 2nd check: if neither original URL nor filename had an extension, we can safely
		// add .jpg here since we know from the content type detection that it's image/jpeg
		if !utils.HasExtension(uploadRequest.Key) {
			newExt := ".jpg"
			h.log.Printf("%s has not extension, adding %s based on mimetype %s", uploadRequest.Key, newExt, contentType)
			uploadRequest.Key = uploadRequest.Key + newExt
			errRename := os.Rename(uploadRequest.LocalPath, uploadRequest.LocalPath+newExt)
			if errRename != nil {
				h.log.Printf("Cannot add suffix %s to %s: %v", newExt, uploadRequest.LocalPath, errRename)
			} else {
				uploadRequest.LocalPath = uploadRequest.LocalPath + newExt
			}
		}

	} else if utils.IsMP3(contentType) {
		tags, _ := audio.ExtractTags(uploadRequest.LocalPath)
		for key, element := range tags {
			tagMap[key] = element
		}
	}

	taggingStr := encodeTagMap(tagMap)
	h.log.Printf("requestId=%s path=%s tags=%v", uploadRequest.RequestId, uploadRequest.LocalPath, *taggingStr)

	// delete actual s3 upload to function
	uploadErr := h.uploadToS3(uploadRequest.LocalPath, uploadRequest.Key, contentType, *taggingStr)
	if uploadErr != nil {
		h.log.Error().Msgf("S3.Upload - localPath: %s, err: %v", uploadRequest.LocalPath, uploadErr)
		return uploadErr
	}

	// if it's an Image, let's create some resized versions of it ...
	if utils.IsResizableImage(contentType) {

		resizeResponse := image.ResizeImage(uploadRequest.LocalPath, h.config.ResizeModes, h.config.ResizeQuality)
		for resizeMode, resizedFilePath := range resizeResponse {

			tagging := fmt.Sprintf("ResizeMode=%s", resizeMode)
			dir, origFile := filepath.Split(uploadRequest.Key)
			resizeKey := fmt.Sprintf("%s%s/%s", dir, resizeMode, origFile)
			if err = h.uploadToS3(resizedFilePath, resizeKey, contentType, tagging); err != nil {
				h.log.Error().Msgf("WARN: Could not upload file %s as key %s: %v", resizedFilePath, resizeKey, err)
			}

			if err = os.Remove(resizedFilePath); err != nil {
				h.log.Warn().Msgf("WARN: Could not delete resized file %s: %v", uploadRequest.LocalPath, err)
			}
		}
	}

	// Publish Kafka event to notify the arrival of a new imagine eventEntity
	eventMsg := fmt.Sprintf("Uploaded %s key=%s size=%d", uploadRequest.LocalPath, uploadRequest.Key, uploadRequest.Size)
	// we should get a bit more smart / flexible here
	eventEntity := "image"
	if utils.IsMP3(contentType) {
		eventEntity = "song"
	}
	event := h.publisher.NewEvent("create:"+eventEntity, eventMsg)
	event.EntityId = uploadRequest.EntityId
	if _, _, err = h.publisher.PublishEvent(event, h.config.KafkaTopic); err != nil {
		h.log.Warn().Msgf("WARN: Cannot Publish event to %s: %v", h.config.KafkaTopic, err)
	}

	// All good, let's remove the temporary file
	rmErr := os.Remove(uploadRequest.LocalPath)
	if rmErr != nil {
		h.log.Printf("WARN: Could not delete temp file %s: %v", uploadRequest.LocalPath, rmErr)
	}

	if h.config.ForceGc {
		h.log.Printf("ForceGC active, trying to free memory")
		// FreeOSMemory forces a garbage collection followed by an
		// attempt to return as much memory to the operating system
		debug.FreeOSMemory()
		h.log.Printf("Memstats %s", utils.MemStats())
	}
	return err
}

func (h *Handler) uploadToS3(filepath string, key string, contentType string, tagging string) error {
	fileHandle, err := os.Open(filepath)
	if err != nil {
		h.log.Error().Msgf("os.Open for upload failed, localFileLocation: %s, err: %v", filepath, err)
		return err
	}
	defer utils.CheckedClose(fileHandle)

	start := time.Now()
	res, uploadErr := s3.New(h.session).PutObject(&s3.PutObjectInput{
		Bucket:             aws.String(h.config.S3Bucket),
		Key:                aws.String(key),      // full S3 object key.
		Body:               fileHandle,           // bytes.NewReader(buffer),
		ContentDisposition: aws.String("inline"), /* or attachment */
		ContentType:        aws.String(contentType),
		StorageClass:       aws.String(s3.ObjectStorageClassStandardIa),
		Tagging:            aws.String(tagging),
		// ACL:                aws.String(S3_ACL),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ServerSideEncryption: aws.String("AES256"),
	})
	elapsed := time.Since(start) / time.Millisecond
	if uploadErr != nil {
		h.log.Error().Msgf("could not upload  %s: %v", key, uploadErr)
	} else {
		h.log.Printf("s3.New: s3://%v/%v elapsed=%dms contentType=%s ETag=%v ", h.config.S3Bucket, key, elapsed, contentType, res.ETag)
	}
	return uploadErr
}

// ListFolders gets a list of "folders" (more accurately referred to as commonPrefixes) from S3
func (h *Handler) ListFolders(rootFolder string) ([]types.ListItem, error) {
	params := &s3.ListObjectsV2Input{
		Bucket:    aws.String(h.config.S3Bucket),
		Prefix:    aws.String(rootFolder),
		Delimiter: aws.String("/"),
	}
	s3client := s3.New(h.session)
	resp, err := s3client.ListObjectsV2(params)
	if err != nil {
		return nil, err
	}
	items := make([]types.ListItem, len(resp.CommonPrefixes))
	emptyTags := make(map[string]string)
	for i, cp := range resp.CommonPrefixes {
		items[i] = types.ListItem{
			Path:     "/" + *cp.Prefix,
			Filename: strings.TrimSuffix(strings.TrimPrefix(*cp.Prefix, rootFolder), "/"),
			Tags:     emptyTags,
		}
		//fmt.Printf("CommonPrefix %v", cp)
	}
	return items, nil
}

// ListObjectsForEntity gets a list of object from S3 (with tagMap)
func (h *Handler) ListObjectsForEntity(prefix string) (types.ListResponse, error) {
	params := &s3.ListObjectsInput{
		Bucket: aws.String(h.config.S3Bucket),
		Prefix: aws.String(prefix),
	}
	s3client := s3.New(h.session)
	resp, err := s3client.ListObjects(params)
	if err != nil {
		return types.ListResponse{}, err
	}
	var items []types.ListItem // make([]ListItem, len(resp.Contents))
ListLoop:
	for _, key := range resp.Contents {
		// check https://stackoverflow.com/questions/38051789/listing-files-in-a-specific-folder-of-a-aws-s3-bucket
		// maybe we can exclude "folders" already in the request
		filename := strings.TrimPrefix(*key.Key, prefix+"/")
		// if key starts with resize dir, skip (would be better for filter out resize dir in the first place)
		for resizeMode := range h.config.ResizeModes {
			if strings.HasPrefix(filename, resizeMode+"/") {
				continue ListLoop
			}
		}

		got, _ := s3client.GetObjectTagging(&s3.GetObjectTaggingInput{
			Bucket: aws.String(h.config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		tagMap := make(map[string]string)
		tags := got.TagSet
		for i := range tags {
			tagMap[*tags[i].Key] = *tags[i].Value

		}
		// #39 add ContentType for older items based on https://golang.org/src/mime/type.go?s=2843:2882#L98
		// TypeByExtension returns the MIME type associated with the file extension ext.
		if _, ok := tagMap[TagContentType]; !ok {
			mimeTypeByExt := mime.TypeByExtension(filepath.Ext(filename))
			if mimeTypeByExt == "" {
				h.log.Warn().Msgf("tag %s was unset, and could not be guessed from %s", TagContentType, filename)
			} else {
				tagMap[TagContentType] = mimeTypeByExt
			}
		}
		items = append(items, types.ListItem{Filename: filename, Path: "/" + *key.Key, Tags: tagMap})
	}
	h.log.Printf("found %d items for id prefix %s", len(items), prefix)
	lr := types.ListResponse{Items: items}
	return lr, nil
}

// GetS3PreSignedUrl creates a pre-signed url for direct download from bucket
func (h *Handler) GetS3PreSignedUrl(key string) string {

	// Construct a GetObjectRequest request
	req, _ := s3.New(h.session).GetObjectRequest(&s3.GetObjectInput{
		Bucket: aws.String(h.config.S3Bucket),
		Key:    aws.String(key),
	})

	// Presign with expiration time
	preSignedUrl, err := req.Presign(h.config.PresignExpiry)

	// Check if it can be signed or not
	if err != nil {
		fmt.Println("Failed to sign request", err)
	}
	h.log.Printf("created presign for key %s with expiry %v", key, h.config.PresignExpiry)

	return preSignedUrl
}

// DownloadObject downloads a file from S3
func (h *Handler) DownloadObject(key string) (string, error) {
	tmpFile := filepath.Join(h.config.Dumpdir, xid.New().String()+filepath.Ext(key))
	// Create the file
	newFile, err := os.Create(tmpFile)
	if err != nil {
		return tmpFile, err
	}
	defer utils.CheckedClose(newFile)

	downloader := s3manager.NewDownloader(h.session)
	numBytes, err := downloader.Download(newFile, &s3.GetObjectInput{
		Bucket: aws.String(h.config.S3Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		h.log.Err(err).Msg(err.Error())
	} else {
		h.log.Debug().Msgf("Downloaded %d bytes to %s", numBytes, tmpFile)
	}
	return tmpFile, err
}

func (h *Handler) PutTags(key string, tagMap map[string]string) error {
	s3TagSet := make([]*s3.Tag, 0, len(tagMap))
	for k := range tagMap {
		val := sanitizeTagValue(tagMap[k])
		s3TagSet = append(s3TagSet, &s3.Tag{
			Key:   aws.String(sanitizeTagValue(k)),
			Value: aws.String(val),
		})
	}
	input := &s3.PutObjectTaggingInput{
		Bucket:  aws.String(h.config.S3Bucket),
		Key:     aws.String(key),
		Tagging: &s3.Tagging{TagSet: s3TagSet},
	}
	_, err := s3.New(h.session).PutObjectTagging(input)
	if err != nil {
		return err
	}
	h.log.Debug().Msgf("Updated %d tags for %s", len(s3TagSet), key)
	return nil
}

// encodeTagMap makes sure that the map of key value pairs is properly encoded as String
// map keys will be sorted in alphabetical order to ease testing (ensure predictable string)
func encodeTagMap(tagMap map[string]string) *string {
	var tagging strings.Builder
	cnt := 0

	// guarantee predictable tag order https://yourbasic.org/golang/sort-map-keys-values/
	tagKeys := make([]string, 0, len(tagMap))
	for k := range tagMap {
		tagKeys = append(tagKeys, k)
	}
	sort.Strings(tagKeys)

	for _, key := range tagKeys {
		tagValue := tagMap[key]
		// some urls may be already escaped, in which case AWS throws and exception when using double escaped values
		// e.g. something%C3%B-else-und-Tips-f%C3%BCr-Something-78.jpg
		// so if we can successfully unescape it, we  unescape it first value and re-escape it later once we've t
		// performed our own sanitization and processing
		tagValUnescaped, err := url.QueryUnescape(tagValue)
		if tagValUnescaped != tagValue && err == nil {
			log.Warn().Msgf("%s was already escaped, re-escaped with unescaped value %s", tagValue, tagValUnescaped)
			tagValue = tagValUnescaped
		}
		tagValue = sanitizeTagValue(tagValue) // remove special stuff
		tagging.WriteString(fmt.Sprintf("%s=%s", key, url.QueryEscape(tagValue)))
		cnt++
		if cnt < len(tagMap) {
			tagging.WriteString("&")
		}
	}
	tagString := tagging.String()
	return &tagString
}

// sanitizeTagValue makes sure only valid characters appear in the tag value
// See https://stackoverflow.com/a/69399728/4292075 and
// https://docs.aws.amazon.com/directoryservice/latest/devguide/API_Tag.html
func sanitizeTagValue(tagVal string) string {
	// \p{L} matches a single code point in the category "letter".
	// \p{N} matches any kind of numeric character in any script.
	re := regexp.MustCompile(`[^\p{L}\p{Z}\p{N}_.:/=+\-@]+`)
	// replace common invalid chars that have a meaningful alias (+ does not work with unescape fix)
	sanitizedTagVal := strings.ReplaceAll(tagVal, "&", "+")
	// for the rest, simply remove
	sanitizedTagVal = re.ReplaceAllString(sanitizedTagVal, "")
	// Length Constraints: Minimum length of 0. Maximum length of 256.
	maxLen := 256
	if len(sanitizedTagVal) > maxLen {
		sanitizedTagVal = sanitizedTagVal[:maxLen]
		log.Debug().Msgf("Initial tag value of len '%d' trimmed to maxLen=%d", len(tagVal), maxLen)
	}
	return sanitizedTagVal
}
