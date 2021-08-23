package main

import (
	"fmt"
	"mime"
	"net/http"
	"net/url"
	"path/filepath"
	"runtime/debug"
	"strconv"
	"strings"
	"time"

	"github.com/tillkuhn/angkor/tools/topkapi"

	"os"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
)

const TagContentType = "ContentType"

type S3Handler struct {
	Session   *session.Session
	Publisher *topkapi.Client
}

// StartWorker invokes as goroutine to listen for new upload requests
func (h S3Handler) StartWorker(jobChan <-chan UploadRequest) {
	for job := range jobChan {
		logger.Printf("Process uploadJob %v", job)
		err := h.PutObject(&job)
		if err != nil {
			logger.Printf("ERROR: PutObject - filename: %v, err: %v", job.LocalPath, err)
		}
		logger.Printf("PutObject id=%s - success", job.RequestId)
	}
}

// PutObject Puts a new object into s3 bucket, inspired by
// https://golangcode.com/uploading-a-file-to-s3/
// https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/go/example_code/s3
func (h S3Handler) PutObject(uploadRequest *UploadRequest) error {
	fileHandle, err := os.Open(uploadRequest.LocalPath)
	// Only the first 512 bytes are used to sniff the content type.
	buffer := make([]byte, 512)
	_, errb := fileHandle.Read(buffer)
	if errb != nil {
		return err
	}
	contentType := http.DetectContentType(buffer) // make a guess, or return application/octet-stream
	// fileHandle.Seek(0, io.SeekStart) // rewind my selector
	fileHandle.Close()

	// init s3 tags, for jpeg content type parse exif and store in s3 tags
	tagMap := make(map[string]string)
	tagMap[TagContentType] = contentType
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
			logger.Printf("%s has not extension, adding %s based on mimetype %s", uploadRequest.Key, newExt, contentType)
			uploadRequest.Key = uploadRequest.Key + newExt
			errRename := os.Rename(uploadRequest.LocalPath, uploadRequest.LocalPath+newExt)
			if errRename != nil {
				logger.Printf("Cannot add suffix %s to %s: %v", newExt, uploadRequest.LocalPath, errRename)
			} else {
				uploadRequest.LocalPath = uploadRequest.LocalPath + newExt
			}
		}

	}
	taggingStr := encodeTagMap(tagMap)
	logger.Printf("requestId=%s path=%s alltags=%v", uploadRequest.RequestId, uploadRequest.LocalPath, *taggingStr)

	// delete actual s3 upload to function
	uploadErr := h.uploadToS3(uploadRequest.LocalPath, uploadRequest.Key, contentType, *taggingStr)
	if uploadErr != nil {
		logger.Printf("ERROR: S3.Upload - localPath: %s, err: %v", uploadRequest.LocalPath, uploadErr)
		return uploadErr
	}

	// if it's an Image, let's create some resized versions of it ...
	if IsResizableImage(contentType) {

		resizeResponse := ResizeImage(uploadRequest.LocalPath, config.ResizeModes)
		for resizeMode, resizedFilePath := range resizeResponse {

			tagging := fmt.Sprintf("ResizeMode=%s", resizeMode)
			dir, origFile := filepath.Split(uploadRequest.Key)
			resizeKey := fmt.Sprintf("%s%s/%s", dir, resizeMode, origFile)
			h.uploadToS3(resizedFilePath, resizeKey, contentType, tagging)
			// logger.Printf("%v",resizedFilePath)
			rmErr := os.Remove(resizedFilePath)
			if rmErr != nil {
				logger.Printf("WARN: Could not delete resizefile %s: %v", uploadRequest.LocalPath, rmErr)
			}
		}
	}
	eventMsg := fmt.Sprintf("Uploaded %s key=%s size=%d", uploadRequest.LocalPath, uploadRequest.Key,uploadRequest.Size)
	event := h.Publisher.NewEvent("create:image", eventMsg)
	event.EntityId = uploadRequest.EntityId
	h.Publisher.PublishEvent(event, config.KafkaTopic)

	// All good, let's remove the tempfile
	rmErr := os.Remove(uploadRequest.LocalPath)
	if rmErr != nil {
		logger.Printf("WARN: Could not delete tmpfile %s: %v", uploadRequest.LocalPath, rmErr)
	}

	if config.ForceGc {
		logger.Printf("ForceGC active, trying to free memory")
		// FreeOSMemory forces a garbage collection followed by an
		// attempt to return as much memory to the operating system
		debug.FreeOSMemory()
		logger.Printf("Memstats %s", MemStats())
	}
	return err
}

func encodeTagMap(tagmap map[string]string) *string {
	var tagging strings.Builder
	cnt := 0
	for key, element := range tagmap {
		element = strings.ReplaceAll(element, "\"", "") // some have, some don't - let's remove quotes
		// some urls may be already escaped, in which case AWS throws and exception when using double escaped values
		// e.g. Sehensw%C3%BCrdigkeiten-und-Tipps-f%C3%BCr-Visby-78.jpg
		elementUnesc, err := url.QueryUnescape(element)
		if elementUnesc != element && err == nil {
			logger.Printf("%s was already escaped, re-escaped with unescaped value %s", element, elementUnesc)
			element = elementUnesc
		}
		tagging.WriteString(fmt.Sprintf("%s=%s", key, url.QueryEscape(element)))
		cnt++
		if cnt < len(tagmap) {
			tagging.WriteString("&")
		}
	}
	tagstr := tagging.String()
	return &tagstr
}

func (h S3Handler) uploadToS3(filepath string, key string, contentType string, tagging string) error {
	fileHandle, err := os.Open(filepath)
	if err != nil {
		logger.Printf("ERROR: os.Open for upload failed, localFileLocation: %s, err: %v", filepath, err)
		return err
	}
	defer fileHandle.Close()

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
		logger.Printf("ERROR: cannot upload  %s: %v", key, uploadErr)
	} else {
		logger.Printf("s3.New: s3://%v/%v elapsed=%dms contentType=%s ETag=%v ", config.S3Bucket, key, elapsed, contentType, res.ETag)
	}
	return uploadErr
}

/**
 * Get a list of object from S3 (with tagMap)
 */
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
LISTLOOP:
	for _, key := range resp.Contents {
		// check https://stackoverflow.com/questions/38051789/listing-files-in-a-specific-folder-of-a-aws-s3-bucket
		// maybe we can exclude "folders" already in the request
		filename := strings.TrimPrefix(*key.Key, prefix+"/")
		// if key starts with resize dir, skip (would be better for filter out resize dir in the first place)
		for resizeMode := range config.ResizeModes {
			if strings.HasPrefix(filename, resizeMode+"/") {
				continue LISTLOOP
			}
		}

		got, _ := s3client.GetObjectTagging(&s3.GetObjectTaggingInput{
			Bucket: aws.String(config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		tagmap := make(map[string]string)
		tags := got.TagSet
		for i := range tags {
			tagmap[*tags[i].Key] = *tags[i].Value

		}
		// Todo: #39 add ContentType for older itmes based on https://golang.org/src/mime/type.go?s=2843:2882#L98
		if _, ok := tagmap[TagContentType]; !ok {
			mimeTypeByExt := mime.TypeByExtension(filepath.Ext(filename))
			if mimeTypeByExt == "" {
				logger.Printf("WARN: %s tag was  unset, and could be be guessed from %s", TagContentType, filename)
			} else {
				tagmap[TagContentType] = mimeTypeByExt
			}
		}
		items = append(items, ListItem{filename, "/" + *key.Key, tagmap})
	}
	logger.Printf("found %d items for id prefix %s", len(items), prefix)
	lr := ListResponse{Items: items}
	return lr, nil
}

// get a presigned url for direct download from bucket
func (h S3Handler) GetS3PresignedUrl(key string) string {

	// Construct a GetObjectRequest request
	req, _ := s3.New(h.Session).GetObjectRequest(&s3.GetObjectInput{
		Bucket: aws.String(config.S3Bucket),
		Key:    aws.String(key),
	})

	// Presign with expiration time
	presignedUrl, err := req.Presign(config.PresignExpiry)

	// Check if it can be signed or not
	if err != nil {
		fmt.Println("Failed to sign request", err)
	}
	logger.Printf("created presign for key %s with expiry %v", key, config.PresignExpiry)

	// Return the presigned url
	return presignedUrl
}
