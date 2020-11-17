package main

// https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
// https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html
import (
	"encoding/json"
	"fmt"
	"github.com/rs/xid"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strings"
	"time"

	"github.com/gorilla/mux"
)

// receive file from http request, dump to local storage first
func PostObject(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, _ := extractEntityVars(r)
	uploadReq := &UploadRequest{ RequestId: xid.New().String()}

	// TODO validate auth with api, this is only the start
	if config.EnableAuth {
		cookie, err := r.Cookie("JSESSIONID")
		if err != nil {
			handleError(&w,fmt.Sprintf("Cannot validate authSession %v",r.Body),err,http.StatusForbidden)
			return
		}
		log.Printf("Found api session %v, continue", cookie.Value)
	}

	contentType := r.Header.Get("Content-type")/* case insentive, returns "" if not found */
	log.Printf("PostObject requestId=%s path=%v entityType=%v id=%v",
		uploadReq.RequestId, r.URL.Path, entityType, entityId)

	if strings.HasPrefix(contentType,"application/json") { // except JSON formatted download request
		decoder := json.NewDecoder(r.Body)
		var dr DownloadRequest
		err := decoder.Decode(&dr)
		if err != nil {
			handleError(&w,fmt.Sprintf("Cannot parse %v into DownloadRequest",r.Body),err,http.StatusBadRequest)
			return
		}
		if dr.URL == "" {
			handleError(&w,fmt.Sprintf("kex 'url' not found in DownloadRequest %v",dr),err,http.StatusBadRequest)
			return
		}
		uploadReq.Filename,uploadReq.Origin = path.Base(dr.URL),dr.URL
		log.Printf("Triggering download reqeust url=%s filename=%s",dr.URL,uploadReq.Filename)
		uploadReq.LocalPath,uploadReq.Size = downloadFile(dr.URL,uploadReq.Filename)

	} else if strings.HasPrefix(contentType,"multipart/form-data") {
		inMemoryFile, handler, err := r.FormFile(config.Fileparam)
		if err != nil {
			handleError(&w,fmt.Sprintf("Error looking for %s",config.Fileparam),err,http.StatusBadRequest)
			return
		}
		uploadReq.Filename,uploadReq.Origin =handler.Filename,"multipart/form-data"
		uploadReq.LocalPath,uploadReq.Size = copyFileFromMultipart(inMemoryFile,handler.Filename)
	} else {
		handleError(&w,"can only process json or multipart/form-data requests",nil,http.StatusUnsupportedMediaType)
		return
	}

	if uploadReq.Size  < 1 {
		handleError(&w,fmt.Sprintf("UploadRequest %v unexpected dumpsize",uploadReq),nil,http.StatusBadRequest)
		return
	}
	log.Printf("PostObject successfully dumped to temp storage as %s", uploadReq.LocalPath)

	// Push the uploadReq onto the queue.
	uploadReq.Key = fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, uploadReq.Filename)

	// Push the uploadReq onto the queue.
	uploadQueue <- *uploadReq
	log.Printf("S3UploadRequest queued for S3Upload requestId=%s",uploadReq.Key,uploadReq.RequestId )

	w.Header().Set("Content-Type", "application/json")
	uploadRequestJson, err := json.Marshal(uploadReq)
	if err != nil {
		handleError(&w,"cannot marshal request",err,http.StatusInternalServerError)
		return
	}
	w.Write(uploadRequestJson)
}

func copyFileFromMultipart(inMemoryFile multipart.File, filename string) (string, int64){
	defer inMemoryFile.Close()
	//fmt.Fprintf(w, "%v", handler.Header)
	localFilename := filepath.Join(config.Dumpdir, filename)
	localFile, err := os.OpenFile(localFilename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		log.Printf("Error %v",err)
		return "",-1
	}
	defer localFile.Close()
	io.Copy(localFile, inMemoryFile)
	fSize := fileSize(localFile)
	return localFilename,fSize
}

func downloadFile(url string, filename string) (string, int64) {

	filename = path.Base(url)
	localFilename := filepath.Join(config.Dumpdir, filename)
	// Get the data
	resp, err := http.Get(url)
	if err != nil {
		log.Printf("%s",err)
		return "",-1
	}
	defer resp.Body.Close()

	// Create the file
	out, err := os.Create(localFilename)
	if err != nil {
		log.Printf("%s",err)
		return "",-1
	}
	defer out.Close()

	// Write the body to file
	_, err = io.Copy(out, resp.Body)
	if err != nil {
		log.Printf("%s", err)
		return "", -1
	}
	fSize := fileSize(out)
	return localFilename,fSize
}

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

// Get presigned url for  given a path such as places/12345/hase.txt
// support for resized version if ?small, ?medium and ?large request param is present
func GetObjectPresignUrl(w http.ResponseWriter, r *http.Request) {
	// check for ?small etc. request param
	resizePath := parseResizeParams(r)

	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s%s", config.S3Prefix, entityType, entityId, resizePath, item)
	target := s3Handler.GetS3PresignedUrl(key)
	log.Printf("redirecting to key %s with presign url", key)
	http.Redirect(w, r, target,
		// see comments below and consider the codes 308, 302, or 301
		http.StatusTemporaryRedirect)
}

// Get presigned url for  given a path such as places/12345/hase.txt
// support for resized version if ?small, ?medium and ?large request param is present
func DeleteObject(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodDelete {
		http.Error(w, "only supported method is "+http.MethodDelete , http.StatusBadRequest)
		return
	}
	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, item)
	log.Printf("Delete %s to be implemented",key)
	w.WriteHeader(http.StatusNoContent) // send the headers with a 204 response cod
}


// A very simple Hztp Health check.
func Health(w http.ResponseWriter, req *http.Request) {
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

/* helper helper helper helper */

func handleError(writer *http.ResponseWriter, msg string, err error, code int) {
	log.Printf("[ERROR] %s - %v",msg,err)
	http.Error(*writer, fmt.Sprintf("%s - %v",msg,err), code)
}

// get common vars from path variables e.g. /{entityType}/{entityId}/{item}"
func extractEntityVars(r *http.Request) (entityType string, entityId string, key string) {
	vars := mux.Vars(r)
	return vars["entityType"], vars["entityId"], vars["item"] // if not found -> no problem
}

// check for ?small, ?medium etc. request param
func parseResizeParams(r *http.Request) string {
	parseErr := r.ParseForm()
	if parseErr != nil {
		log.Printf("WARN: Cannot parse request URI %s", r.RequestURI)
		return ""
	}
	for resizeMode, _ := range config.ResizeModes {
		_, isRequested := r.Form[resizeMode]
		if isRequested {
			return resizeMode + "/"
		}
	}
	return ""
}

func fileSize(file *os.File) int64 {
	fStat, err := file.Stat()
	if err != nil {
		// Could not obtain stat, handle error
		log.Printf("WARNIG Cannot obtain filesize: %v",err)
		return -1
	}
	return fStat.Size()
}
