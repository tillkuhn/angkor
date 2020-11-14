package main

// https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
// https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html
import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/gorilla/mux"
	"github.com/rs/xid"
)

/* Get a list of objects given a path such as places/12345 */
func ListObjects(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, _ := extractEntityVars(r)
	prefix := fmt.Sprintf("%s%s/%s", config.S3Prefix, entityType, entityId)
	lr, _ := s3Handler.ListObjectsForEntity(prefix)
	// https://stackoverflow.com/questions/28595664/how-to-stop-json-marshal-from-escaping-and/28596225
	w.Header().Set("Content-Type", "application/json")
	enc := json.NewEncoder(w)
	enc.SetEscapeHTML(false) // or & will be escaped with unicode chars
	if err := enc.Encode(&lr.Items); err != nil {
		log.Println(err)
	}
}

/* Get presigned url for  given a path such as places/12345/hase.txt */
func GetObjectPresignUrl(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, item)
	target := s3Handler.GetS3PresignedUrl(key)
	log.Printf("redirecting %s with presign url", key)
	http.Redirect(w, r, target,
		// see comments below and consider the codes 308, 302, or 301
		http.StatusTemporaryRedirect)
}

func GetObjectThumbnailPresignUrl(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s/%s", config.S3Prefix, entityType, entityId,config.Thumbsubdir, item)
	target := s3Handler.GetS3PresignedUrl(key)
	log.Printf("redirecting %s with presign url", key)
	http.Redirect(w, r, target,
		// see comments below and consider the codes 308, 302, or 301
		http.StatusTemporaryRedirect)
}

/* receive file from http request, dump to local storage first */
func UploadObject(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, _ := extractEntityVars(r)
	log.Printf("method: %v path: %v entityType: %v id %v\\", r.Method, r.URL.Path, entityType, entityId)
	r.ParseMultipartForm(32 << 20)
	uploadFile, handler, err := r.FormFile(config.Fileparam)
	if err != nil {
		fmt.Println("error looking for", config.Fileparam, err)
		return
	}
	defer uploadFile.Close()
	//fmt.Fprintf(w, "%v", handler.Header)
	localFilename := filepath.Join(config.Dumpdir, handler.Filename)
	f, err := os.OpenFile(localFilename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		fmt.Println(err)
		return
	}

	defer f.Close()
	io.Copy(f, uploadFile)
	fStat, err := f.Stat()
	if err != nil {
		// Could not obtain stat, handle error
	}
	fSize := fStat.Size()
	log.Printf("Uploaded %s dumped to temp storage %s", handler.Filename, localFilename)

	// Push the uploadReq onto the queue.
	uploadReq := UploadRequest{
		LocalPath: localFilename,
		Key:       fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, handler.Filename),
		Size:      fSize,
		RequestId: xid.New().String(),
	}

	// Push the uploadReq onto the queue.
	uploadQueue <- uploadReq
	log.Printf("S3UploadRequest queued for %s", localFilename)

	w.Header().Set("Content-Type", "application/json")
	status, err := json.Marshal(uploadReq)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Write(status)
}

// A very simple health check.
func health(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	status, err := json.Marshal(map[string]interface{}{
		"status": "up",
		"info":   fmt.Sprintf("%s is healthy", appPrefix),
		"time":   time.Now().Format(time.RFC3339),
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Write(status)
}

// helper
func extractEntityVars(r *http.Request) (entityType string, entityId string, key string) {
	vars := mux.Vars(r)
	return vars["entityType"], vars["entityId"], vars["item"]
}

//if r.Method == "GET" {
//crutime := time.Now().Unix()
//h := md5.New()
//io.WriteString(h, strconv.FormatInt(crutime, 10))
//token := fmt.Sprintf("%x", h.Sum(nil))
//
//t, _ := template.ParseFiles("upload.gtpl")
//t.Execute(w, token)
//} else {
