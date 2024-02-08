package server

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
	"github.com/tillkuhn/angkor/tools/imagine/auth"
	"github.com/tillkuhn/angkor/tools/imagine/types"
	"github.com/tillkuhn/angkor/tools/imagine/utils"

	"github.com/rs/xid"
)

func TestMemStats(t *testing.T) {
	result := utils.MemStats()
	if !strings.Contains(result, "TotalAlloc") {
		t.Errorf("TestMemStats() = %v; expect to contain %v", result, "TotalAlloc")
	}
}

func TestXID(t *testing.T) {
	result := xid.New().String() // e.g. c1m8eof2anc66efg
	if len(result) <= 8 {
		t.Errorf("TextXID() %v invalid length should be min 8", result)
	}
}

// sample usage
func TestShouldRejectPostIfUnauthenticated(t *testing.T) {
	if err := os.Setenv("IMAGINE_S3BUCKET", "s3://test"); err != nil {
		assert.Fail(t, err.Error())
	}
	if err := os.Setenv("IMAGINE_ENABLE_AUTH", "true"); err != nil {
		assert.Fail(t, err.Error())
	}
	var config types.Config
	err := envconfig.Process("imagine", &config)
	if err != nil {
		t.Fatal(err)
	}
	authContext := auth.New(config.EnableAuth, config.JwksEndpoint, "4711")
	sh := NewHandler(nil, &config)
	s := httptest.NewServer(authContext.ValidationMiddleware(sh.PostObject))

	defer s.Close()
	targetUrl := s.URL + "/upload/README.md" // e.g. http://127.0.0.1:53049/upload
	fmt.Println(targetUrl)
	filename := "../README.md"
	err = postFile(filename, targetUrl)
	assert.Contains(t, err.Error(), "auth error")
	assert.Contains(t, err.Error(), "403")
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
	defer utils.CheckedClose(fh)
	_, err = io.Copy(fileWriter, fh)
	if err != nil {
		return err
	}

	contentType := bodyWriter.FormDataContentType()
	defer utils.CheckedClose(bodyWriter)

	resp, err := http.Post(targetUrl, contentType, bodyBuf)
	if err != nil {
		return err
	}
	defer utils.CheckedClose(resp.Body)
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	fmt.Printf("Resturns %s", resp.Status)
	// fmt.Println(string(respBody))
	if resp.StatusCode >= 299 {
		return errors.New(resp.Status + ": " +
			string(respBody))
	}
	return nil
}
