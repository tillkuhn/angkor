package main

import (
	"bytes"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"io"
	"net/http"
	"net/url"
	"strings"

	"log"
	"os"
)

const imageContentType = "image/jpeg"

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
	Filename     string            `json:"filename"`
	PresignedUrl string            `json:"presignedUrl"`
	Tags         map[string]string `json:"tags"`
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

	// for jpeg content type parse exif and store in s3 tags
	var tagging strings.Builder
	tagging.WriteString(fmt.Sprintf("size=%d",uploadRequest.Size))
	if contentType == imageContentType {
		exif,_ := extractExif(uploadRequest.LocalPath)
		if len(exif) > 0 {
			tagging.WriteString("&")
			tagging.WriteString(encodeObjectTags(exif))
		}
	}
	log.Printf("alltags %v",tagging.String())

	res, uploadErr := s3.New(h.Session).PutObject(&s3.PutObjectInput{
		Bucket:             aws.String(config.S3Bucket),
		Key:                aws.String(uploadRequest.Key), // full S3 object key.
		Body:               file,                 // bytes.NewReader(buffer),
		ContentDisposition: aws.String("inline"), /* attachment */
		ContentType: aws.String(contentType),
		// ACL:                aws.String(S3_ACL),
		// ContentLength:      aws.Int64(int64(len(buffer))),
		// ServerSideEncryption: aws.String("AES256"),
		Tagging: aws.String(tagging.String()),
	})

	if uploadErr != nil {
		log.Fatalf("S3.Upload - localPath: %s, err: %v", uploadRequest.LocalPath, uploadErr)
	}

	log.Printf("s3.New - res: s3://%v/%v ETag %v contentType=%s", config.S3Bucket, uploadRequest.Key, res.ETag, contentType)
	rmErr := os.Remove(uploadRequest.LocalPath)
	if rmErr != nil {
		log.Printf("Could not delete %s: %v",uploadRequest.LocalPath,rmErr)
	}
	return err
}

func encodeObjectTags(tags map[string]string) string {
	var sb strings.Builder
	cnt :=0
	for key, element := range tags {
		element = strings.ReplaceAll(element,"\"","")
		sb.WriteString(fmt.Sprintf("%s=%s",key,url.QueryEscape(element)))
		cnt =cnt+1
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
	items := make([]ListItem, len(resp.Contents))
	cnt := 0
	for _, key := range resp.Contents {
		path := strings.TrimPrefix(*key.Key,prefix+"/")
		gor, _ := s3client.GetObjectRequest(&s3.GetObjectInput{
			Bucket: aws.String(config.S3Bucket),
			Key:    aws.String(*key.Key),
		})
		presignUrl,_ := gor.Presign(config.PresignExpiry)

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
		items[cnt] = ListItem{path, presignUrl,tagmap}
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
