package main

import (
	"bytes"
	"io"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"

	"log"
	"os"
)

type S3Handler struct {
	Session *session.Session
	Bucket  string
	KeyPrefix  string
}

type UploadRequest struct {
	LocalPath string
	Key string
	RequestId string
	Size int64
	// Delay time.Duration
}

// invoke as goroutine
func worker(jobChan <-chan UploadRequest) {
	for job := range jobChan {
		log.Printf("Process uploadJob %v",job)
		err := s3Handler.UploadFile(&job)
		if err != nil {
			log.Fatalf("UploadFile - filename: %v, err: %v", job.LocalPath, err)
		}
		log.Printf("UploadFile id=%s - success",job.RequestId)
	}
}


/**
 * Put new object into bucket
 */
func (h S3Handler) UploadFile(uploadRequest *UploadRequest) error {
	file, err := os.Open(uploadRequest.LocalPath)
	if err != nil {
		log.Fatalf("os.Open - localFileLocation: %s, err: %v", uploadRequest.LocalPath, err)
	}
	defer file.Close()
	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket: aws.String(h.Bucket),
		Key:    aws.String(uploadRequest.Key),
		Body:               file, // bytes.NewReader(buffer),
		ContentDisposition: aws.String("attachment"),
		// ACL:                aws.String(S3_ACL),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ContentType:        aws.String(http.DetectContentType(buffer)),
		// ServerSideEncryption: aws.String("AES256"),
	})
	if uploadErr != nil {
		log.Fatalf("S3.Upload - localPath: %s, err: %v", uploadRequest.LocalPath, uploadErr)
	}
	log.Printf("s3.New - res: s3://%v/%v ETag %v", h.Bucket, uploadRequest.Key, res.ETag)
	return err
}

/**
 * Get a single object from S3
 */
func (h S3Handler) ReadFile(key string) (string, error) {
	results, err := s3.New(h.Session).GetObject(&s3.GetObjectInput{
		Bucket: aws.String(h.Bucket),
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
