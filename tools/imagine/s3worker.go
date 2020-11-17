package main

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"path/filepath"
	"strings"
	"time"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"log"
	"os"
)

type S3Handler struct {
	Session *session.Session
}

// invoke as goroutine to listen for new upload requests
func (h S3Handler) StartWorker(jobChan <-chan UploadRequest) {
	for job := range jobChan {
		log.Printf("Process uploadJob %v", job)
		err := h.PutObject(&job)
		if err != nil {
			log.Fatalf("PutObject - filename: %v, err: %v", job.LocalPath, err)
		}
		log.Printf("PutObject id=%s - success", job.RequestId)
	}
}

/**
 * Put new object into s3 bucket
 */
func (h S3Handler) PutObject(upreq *UploadRequest) error {
	fileHandle, err := os.Open(upreq.LocalPath)
	// Only the first 512 bytes are used to sniff the content type.
	buffer := make([]byte, 512)
	_, errb := fileHandle.Read(buffer)
	if errb != nil {
		return err
	}
	contentType := http.DetectContentType(buffer)
	// fileHandle.Seek(0, io.SeekStart) // rewind my selector
	fileHandle.Close()
	// https://golangcode.com/uploading-a-file-to-s3/
	// https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/go/example_code/s3

	// for jpeg content type parse exif and store in s3 tags
	var tagging strings.Builder
	tagging.WriteString(fmt.Sprintf("Size=%d&Origin=%s", upreq.Size,upreq.Origin))
	if contentType == imageContentType {
		exif, _ := ExtractExif(upreq.LocalPath)
		if len(exif) > 0 {
			tagging.WriteString("&")
			tagging.WriteString(encodeObjectTags(exif))
		}
	}
	log.Printf("requestId %s %s alltags %v", upreq.RequestId, upreq.LocalPath, tagging.String())

	uploadErr := h.uploadToS3(upreq.LocalPath, upreq.Key, contentType, tagging.String())
	if uploadErr != nil {
		log.Printf("S3.Upload - localPath: %s, err: %v", upreq.LocalPath, uploadErr)
		return uploadErr
	}

	// create thumnails
	if contentType == imageContentType {
		for resizeMode, size := range config.ResizeModes {
			log.Printf("Create %s resized version size %d", resizeMode, size)
			resizedFilePath := ResizeImage(upreq.LocalPath, size)
			tagging := fmt.Sprintf("ResizeMode=%s", resizeMode)
			dir, origfile := filepath.Split(upreq.Key)
			resizdeKey := fmt.Sprintf("%s%s/%s", dir, resizeMode, origfile)
			h.uploadToS3(resizedFilePath, resizdeKey, contentType, tagging)
			// log.Printf("%v",resizedFilePath)
			rmErr := os.Remove(resizedFilePath)
			if rmErr != nil {
				log.Printf("WARN: Could not delete resizefile %s: %v", upreq.LocalPath, rmErr)
			}
		}
	}

	// All good, let's remove the tempfile
	rmErr := os.Remove(upreq.LocalPath)
	if rmErr != nil {
		log.Printf("WARN: Could not delete %s: %v", upreq.LocalPath, rmErr)
	}
	return err
}

func (h S3Handler) uploadToS3(filepath string, key string, contentType string, tagging string) error {
	fileHandle, err := os.Open(filepath)
	if err != nil {
		log.Fatalf("os.Open - localFileLocation: %s, err: %v", filepath, err)
	}
	defer fileHandle.Close()

	start := time.Now()
	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket:             aws.String(config.S3Bucket),
		Key:                aws.String(key),      // full S3 object key.
		Body:               fileHandle,           // bytes.NewReader(buffer),
		ContentDisposition: aws.String("inline"), /* attachment */
		ContentType:        aws.String(contentType),
		StorageClass:       aws.String(s3.ObjectStorageClassStandardIa),
		Tagging:            aws.String(tagging),
		// ACL:                aws.String(S3_ACL),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ServerSideEncryption: aws.String("AES256"),
	})
	elapsed := time.Since(start) / time.Second
	if uploadErr != nil {
		log.Printf("Error creating %s: %v", key, uploadErr)
	} else {
		log.Printf("s3.New: s3://%v/%v elapsed=%ds contentType=%s ETag=%v ", config.S3Bucket, key, elapsed, contentType, res.ETag)
	}
	return uploadErr
}

func encodeObjectTags(tags map[string]string) string {
	var sb strings.Builder
	cnt := 0
	for key, element := range tags {
		element = strings.ReplaceAll(element, "\"", "")
		sb.WriteString(fmt.Sprintf("%s=%s", key, url.QueryEscape(element)))
		cnt = cnt + 1
		if cnt < len(tags) {
			sb.WriteString("&")
		}
	}
	return sb.String()
}

/**
 * Get a list of object from S3
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
	items := []ListItem{} // make([]ListItem, len(resp.Contents))
LISTLOOP:
	for _, key := range resp.Contents {
		// check https://stackoverflow.com/questions/38051789/listing-files-in-a-specific-folder-of-a-aws-s3-bucket
		// maybe we can exclude "folders" already in the request
		filename := strings.TrimPrefix(*key.Key, prefix+"/")
		// if key starts with resize dir, skip
		for resizeMode, _ := range config.ResizeModes {
			if strings.HasPrefix(filename, resizeMode+"/") {
				continue LISTLOOP
			}
		}

		// we no longer need presign
		//gor, _ := s3client.GetObjectRequest(&s3.GetObjectInput{
		//	Bucket: aws.String(config.S3Bucket),
		//	Key:    aws.String(*key.Key),
		//})
		//presignUrl, _ := gor.Presign(config.PresignExpiry)

		got, _ := s3client.GetObjectTagging(&s3.GetObjectTaggingInput{
			Bucket: aws.String(config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		tagmap := make(map[string]string)
		tags := got.TagSet
		for i := range tags {
			tagmap[*tags[i].Key] = *tags[i].Value

		}
		// log.Printf("%v",tags)
		//items[cnt] = ListItem{filename, presignUrl, tagmap}
		items = append(items, ListItem{filename, "/" + *key.Key, tagmap})
	}
	log.Printf("found %d items for id prefix %s", len(items), prefix)
	lr := ListResponse{Items: items}
	return lr, nil
}

/**
 * Get a single object from S3
 */
func (h S3Handler) getObject(key string) (string, error) {
	results, err := s3.New(h.Session).GetObject(&s3.GetObjectInput{
		Bucket: aws.String(config.S3Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		return "", err
	}
	defer results.Body.Close()

	buf := bytes.NewBuffer(nil)
	if _, err := io.Copy(buf, results.Body); err != nil {
		return "", err
	}
	return string(buf.Bytes()), nil
}

/**
 * get a presigned url for direct download from bucket
 */
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
	log.Printf("created presign for key %s with expiry %v", key, config.PresignExpiry)

	// Return the presigned url
	return presignedUrl
}
