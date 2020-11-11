package main

import (
	"bytes"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"io"
	"net/http"
	"strings"

	"log"
	"os"
)

type S3Handler struct {
	Session   *session.Session
}

type UploadRequest struct {
	LocalPath string
	Key       string
	RequestId string
	Size      int64
	// Delay time.Duration
}

type ListResponse struct {
	Items []ListItem `json:"items"`
}

type ListItem struct {
	Path string `json:"path"`
	URL string `json:"url"`
}

// invoke as goroutine
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
 * Put new object into bucket
 */
func (h S3Handler) PutObject(uploadRequest *UploadRequest) error {
	file, err := os.Open(uploadRequest.LocalPath)
	if err != nil {
		log.Fatalf("os.Open - localFileLocation: %s, err: %v", uploadRequest.LocalPath, err)
	}
	defer file.Close()
	// Only the first 512 bytes are used to sniff the content type.
	buffer := make([]byte, 512)

	_, errb := file.Read(buffer)
	if errb != nil {
		return err
	}
	contentType := http.DetectContentType(buffer)
	file.Seek(0, io.SeekStart) // rewind my selector
	// https://golangcode.com/uploading-a-file-to-s3/
	// https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/go/example_code/s3
	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket:             aws.String(config.S3Bucket),
		Key:                aws.String(uploadRequest.Key), // full S3 object key.
		Body:               file,                 // bytes.NewReader(buffer),
		ContentDisposition: aws.String("inline"), /* attachment */
		ContentType: aws.String(contentType),
		// ACL:                aws.String(S3_ACL),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ServerSideEncryption: aws.String("AES256"),
		Tagging: aws.String("horst=klaus&fx=nasenbaer"),
	})

	if uploadErr != nil {
		log.Fatalf("S3.Upload - localPath: %s, err: %v", uploadRequest.LocalPath, uploadErr)
	}
	log.Printf("s3.New - res: s3://%v/%v ETag %v contentType=%s", config.S3Bucket, uploadRequest.Key, res.ETag, contentType)
	return err
}

/**
 * Get a single object from S3
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
	items := make([]ListItem, len(resp.Contents))
	cnt := 0
	for _, key := range resp.Contents {
		path := strings.TrimPrefix(*key.Key, config.S3Prefix)
		gor, _ := s3client.GetObjectRequest(&s3.GetObjectInput{
			Bucket: aws.String(config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		url,_ := gor.Presign(config.PresignExpiry)
		items[cnt] = ListItem{path,url}
		cnt = cnt + 1
	}
	log.Printf("found %d items for id prefix %s", cnt, prefix)
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
