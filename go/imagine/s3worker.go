package main

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

type S3Handler struct {
	Session   *session.Session
	Publisher *topkapi.Client
	log       zerolog.Logger
}

// StartWorker invokes as goroutine to listen for new upload requests
func (h S3Handler) StartWorker(jobChan <-chan UploadRequest) {
	for job := range jobChan {
		h.log.Printf("Process uploadJob %v", job)
		err := h.PutObject(&job)
		if err != nil {
			h.log.Error().Msgf("PutObject - filename: %v, err: %v", job.LocalPath, err)
		}
		h.log.Printf("PutObject id=%s - success", job.RequestId)
	}
}

// PutObject Puts a new object into s3 bucket, inspired by
// - https://golangcode.com/uploading-a-file-to-s3/
// - https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/go/example_code/s3
func (h S3Handler) PutObject(uploadRequest *UploadRequest) error {
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
	defer checkedClose(fileHandle)
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
	tagMap["Origin"] = StripRequestParams(uploadRequest.Origin) // even if encoded, ?bla=bla parts raise exceptions
	// EXIF tags can be only extracted for image/jpeg files
	if IsJPEG(contentType) {
		exif, _ := ExtractExif(uploadRequest.LocalPath)
		// merge extracted exif tags into master tagMap
		if len(exif) > 0 {
			for key, element := range exif {
				tagMap[key] = element
			}
		}
		// 2nd check: if neither original URL nor filename had an extension, we can safely
		// add .jpg here since we know from the content type detection that it's image/jpeg
		if !HasExtension(uploadRequest.Key) {
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

	} else if IsMP3(contentType) {
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
	if IsResizableImage(contentType) {

		resizeResponse := ResizeImage(uploadRequest.LocalPath, config.ResizeModes)
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
	eventMsg := fmt.Sprintf("Uploaded %s key=%s size=%d", uploadRequest.LocalPath, uploadRequest.Key, uploadRequest.Size)
	event := h.Publisher.NewEvent("create:image", eventMsg)
	event.EntityId = uploadRequest.EntityId
	if _, _, err = h.Publisher.PublishEvent(event, config.KafkaTopic); err != nil {
		h.log.Warn().Msgf("WARN: Cannot Publish event to %s: %v", config.KafkaTopic, err)
	}

	// All good, let's remove the temporary file
	rmErr := os.Remove(uploadRequest.LocalPath)
	if rmErr != nil {
		h.log.Printf("WARN: Could not delete temp file %s: %v", uploadRequest.LocalPath, rmErr)
	}

	if config.ForceGc {
		h.log.Printf("ForceGC active, trying to free memory")
		// FreeOSMemory forces a garbage collection followed by an
		// attempt to return as much memory to the operating system
		debug.FreeOSMemory()
		h.log.Printf("Memstats %s", MemStats())
	}
	return err
}

func (h S3Handler) uploadToS3(filepath string, key string, contentType string, tagging string) error {
	fileHandle, err := os.Open(filepath)
	if err != nil {
		h.log.Error().Msgf("os.Open for upload failed, localFileLocation: %s, err: %v", filepath, err)
		return err
	}
	defer checkedClose(fileHandle)

	start := time.Now()
	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket:             aws.String(config.S3Bucket),
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
		h.log.Printf("s3.New: s3://%v/%v elapsed=%dms contentType=%s ETag=%v ", config.S3Bucket, key, elapsed, contentType, res.ETag)
	}
	return uploadErr
}

// ListObjectsForEntity gets a list of object from S3 (with tagMap)
func (h S3Handler) ListObjectsForEntity(prefix string) (ListResponse, error) {
	params := &s3.ListObjectsInput{
		Bucket: aws.String(config.S3Bucket),
		Prefix: aws.String(prefix),
	}
	s3client := s3.New(h.Session)
	resp, err := s3client.ListObjects(params)
	if err != nil {
		return ListResponse{}, err
	}
	var items []ListItem // make([]ListItem, len(resp.Contents))
ListLoop:
	for _, key := range resp.Contents {
		// check https://stackoverflow.com/questions/38051789/listing-files-in-a-specific-folder-of-a-aws-s3-bucket
		// maybe we can exclude "folders" already in the request
		filename := strings.TrimPrefix(*key.Key, prefix+"/")
		// if key starts with resize dir, skip (would be better for filter out resize dir in the first place)
		for resizeMode := range config.ResizeModes {
			if strings.HasPrefix(filename, resizeMode+"/") {
				continue ListLoop
			}
		}

		got, _ := s3client.GetObjectTagging(&s3.GetObjectTaggingInput{
			Bucket: aws.String(config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		tagMap := make(map[string]string)
		tags := got.TagSet
		for i := range tags {
			tagMap[*tags[i].Key] = *tags[i].Value

		}
		// Todo: #39 add ContentType for older items based on https://golang.org/src/mime/type.go?s=2843:2882#L98
		if _, ok := tagMap[TagContentType]; !ok {
			mimeTypeByExt := mime.TypeByExtension(filepath.Ext(filename))
			if mimeTypeByExt == "" {
				h.log.Printf("WARN: %s tag was  unset, and could be be guessed from %s", TagContentType, filename)
			} else {
				tagMap[TagContentType] = mimeTypeByExt
			}
		}
		items = append(items, ListItem{filename, "/" + *key.Key, tagMap})
	}
	h.log.Printf("found %d items for id prefix %s", len(items), prefix)
	lr := ListResponse{Items: items}
	return lr, nil
}

// GetS3PreSignedUrl creates a pre-signed url for direct download from bucket
func (h S3Handler) GetS3PreSignedUrl(key string) string {

	// Construct a GetObjectRequest request
	req, _ := s3.New(h.Session).GetObjectRequest(&s3.GetObjectInput{
		Bucket: aws.String(config.S3Bucket),
		Key:    aws.String(key),
	})

	// Presign with expiration time
	preSignedUrl, err := req.Presign(config.PresignExpiry)

	// Check if it can be signed or not
	if err != nil {
		fmt.Println("Failed to sign request", err)
	}
	h.log.Printf("created presign for key %s with expiry %v", key, config.PresignExpiry)

	return preSignedUrl
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
		element := sanitizeTagValue(tagMap[key])
		// some urls may be already escaped, in which case AWS throws and exception when using double escaped values
		// e.g. something%C3%B-else-und-Tips-f%C3%BCr-Something-78.jpg
		elementUnescaped, err := url.QueryUnescape(element)
		if elementUnescaped != element && err == nil {
			log.Warn().Msgf("%s was already escaped, re-escaped with unescaped value %s", element, elementUnescaped)
			element = elementUnescaped
		}
		tagging.WriteString(fmt.Sprintf("%s=%s", key, url.QueryEscape(element)))
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
	re := regexp.MustCompile(`[^\p{L}\p{Z}\p{N}_.:/=+\-@]+`)
	// replace common invalid chars that have a meaningful alias (+ does not work with unescape fix)
	// sTagVal := strings.ReplaceAll(tagVal, "&", "+")
	// for the rest, simply remove
	sTagVal := re.ReplaceAllString(tagVal, "")
	// Length Constraints: Minimum length of 0. Maximum length of 256.
	if len(sTagVal) > 256 {
		sTagVal = sTagVal[:256]
	}
	if len(tagVal) != len(sTagVal) {
		log.Debug().Msgf("Initial tag value '%s' sanitized to '%s'", tagVal, sTagVal)
	}
	return sTagVal
}
