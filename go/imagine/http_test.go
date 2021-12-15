package main

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"

	"github.com/kelseyhightower/envconfig"
	"github.com/stretchr/testify/assert"

	"github.com/rs/xid"
)

func TestMemStats(t *testing.T) {
	result := MemStats()
	if !strings.Contains(result, "TotalAlloc") {
		t.Errorf("TestMemStats() = %v; expect to contain %v", result, "TotalAlloc")
	}
}

func TestXID(t *testing.T) {
	result := xid.New().String() // e.g. c1m8eof2hran66ddldlg
	if len(result) <= 8 {
		t.Errorf("TextXID() %v invalid length should be min 8", result)
	}
}

// sample usage
func TestShouldRejectPostIfUnauthenticated(t *testing.T) {
	os.Setenv("IMAGINE_S3BUCKET", "s3://test")
	err := envconfig.Process(AppId, &config)
	if err != nil {
		t.Fatal(err)
	}
	s := httptest.NewServer(http.HandlerFunc(PostObject))

	defer s.Close()
	targetUrl := s.URL + "/upload/README.md" // e.g. http://127.0.0.1:53049/upload
	fmt.Println(targetUrl)
	filename := "README.md"
	err = postFile(filename, targetUrl)
	assert.Contains(t, err.Error(), "Cannot find/validate X-Authorization header")
}

func postFile(filename string, targetUrl string) error {
	bodyBuf := &bytes.Buffer{}
	bodyWriter := multipart.NewWriter(bodyBuf)

	// this step is very important
	fileWriter, err := bodyWriter.CreateFormFile("uploadfile", filename)
	if err != nil {
		fmt.Println("error writing to buffer")
		return err
	}

	// open file handle
	fh, err := os.Open(filename)
	if err != nil {
		fmt.Println("error opening file")
		return err
	}
	defer checkedClose(fh)
	_, err = io.Copy(fileWriter, fh)
	if err != nil {
		return err
	}

	contentType := bodyWriter.FormDataContentType()
	defer checkedClose(bodyWriter)

	resp, err := http.Post(targetUrl, contentType, bodyBuf)
	if err != nil {
		return err
	}
	defer checkedClose(resp.Body)
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	fmt.Printf("Resturns %s", resp.Status)
	// fmt.Println(string(respBody))
	if resp.StatusCode >= 299 {
		return errors.New(string(respBody))
	}
	return nil
}
