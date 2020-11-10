package main

import (
	"bytes"
	"io"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"

	// "net/http"
	"log"
	"os"
)

type S3Handler struct {
	Session *session.Session
	Bucket  string
	KeyPrefix  string
}

/**
 * Put new object into bucket
 */
func (h S3Handler) UploadFile(key string, filename string) error {
	file, err := os.Open(filename)
	if err != nil {
		log.Fatalf("os.Open - filename: %s, err: %v", filename, err)
	}
	defer file.Close()

	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket: aws.String(h.Bucket),
		Key:    aws.String(h.KeyPrefix + key),
		// ACL:                aws.String(S3_ACL),
		Body:               file, // bytes.NewReader(buffer),
		ContentDisposition: aws.String("attachment"),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ContentType:        aws.String(http.DetectContentType(buffer)),
		// ServerSideEncryption: aws.String("AES256"),
	})
	if uploadErr != nil {
		log.Fatalf("os.Upoload - filename: %s, err: %v", filename, uploadErr)
	}
	log.Printf("s3.New - res: s3://%v/%v ETag %v", h.Bucket, h.KeyPrefix + key, res.ETag)
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
