package main

import (
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strings"
	"time"

	"github.com/gorilla/context"
	"github.com/tillkuhn/angkor/tools/imagine/auth"

	"github.com/rs/zerolog/log"

	"github.com/gorilla/mux"
	"github.com/rs/xid"
)

const (
	ContentTypeJson = "application/json"
	ContentTypeKey  = "Content-Type"
)

// PostSong Dedicated Upload Handler for Songs such as mp3 files
func PostSong(w http.ResponseWriter, r *http.Request) {
	// overwrite mux vars
	vars := mux.Vars(r)
	vars["entityType"] = "songs"
	vars["entityId"] = ""
	PostObject(w, r) // Delegate to standard Post
}

// ListSongs Dedicated List Handler for Songs such as mp3 files
func ListSongs(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	vars["entityType"] = "songs"
	vars["entityId"] = ""
	ListObjects(w, r) // Delegate to standard List
}

// GetSongPresignUrl Returns S3 Download Link for Song
func GetSongPresignUrl(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	item := vars["item"]
	key := fmt.Sprintf("%s%s/%s", config.S3Prefix, "songs", item)
	url := s3Handler.GetS3PreSignedUrl(key)
	log.Printf("Created song presign url for %s: %s", key, url)
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	status, err := json.Marshal(map[string]interface{}{
		"key": key,
		"url": url,
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if _, err := w.Write(status); err != nil {
		log.Error().Msgf("[ERROR] write status %d - %v", status, err)
	}
}

// PostObject extract file from http request (json or multipart)
// dumps it to local storage first, and creates a job for s3 upload
//
// File Upload with Golang: https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
// Worker queues in Go: https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html
//
// URL Pattern: cp+"/{entityType}/{entityId}"
func PostObject(w http.ResponseWriter, r *http.Request) {
	logger := log.Logger.With().Str("logger", "http").Logger()
	entityType, entityId, _ := extractEntityVars(r)
	uploadReq := &UploadRequest{RequestId: xid.New().String(), EntityId: entityId}

	// Looks also promising: https://golang.org/pkg/net/http/#DetectContentType DetectContentType
	// implements the algorithm described at https://mimesniff.spec.whatwg.org/ to determine the Content-Type of the given data.
	contentType := r.Header.Get(ContentTypeKey) /* case-insensitive, returns "" if not found */
	authInfo := context.Get(r, auth.ContextAuthKey).(*auth.TokenInfo)
	logger.Debug().Msgf("PostObject requestId=%s path=%v entityType=%v id=%v authScope=%v",
		uploadReq.RequestId, r.URL.Path, entityType, entityId, authInfo.Scope())

	// distinguish JSON formatted download request and "multipart/form-data"
	if strings.HasPrefix(contentType, ContentTypeJson) {
		decoder := json.NewDecoder(r.Body)
		var dr DownloadRequest
		err := decoder.Decode(&dr) // check if request can be parsed into JSON
		if err != nil {
			handleError(&w, fmt.Sprintf("Cannot parse %v into DownloadRequest", r.Body), err, http.StatusBadRequest)
			return
		}
		if dr.URL == "" {
			handleError(&w, fmt.Sprintf("key 'url' not found in DownloadRequest %v", dr), err, http.StatusBadRequest)
			return
		}
		uploadReq.Origin = dr.URL
		// if request contains a filename, use this instead of url filename but append suffix if not present
		fileExtension := strings.ToLower(StripRequestParams(filepath.Ext(dr.URL))) // .JPG -> .jpg
		if dr.Filename != "" {
			uploadReq.Filename = dr.Filename
			if !HasExtension(uploadReq.Filename) {
				uploadReq.Filename = uploadReq.Filename + fileExtension
				// if the original URL also has no extension, we need to rely on s3worker
				// which detected the Mimetype to fix this
			}
		} else {
			uploadReq.Filename = StripRequestParams(path.Base(dr.URL))
		}
		logger.Printf("Trigger URL DownloadRequest url=%s filename=%s ext=%s", dr.URL, uploadReq.Filename, fileExtension)
		// delegate actual download from URL to downloadFile
		uploadReq.LocalPath, uploadReq.Size = downloadFile(dr.URL, uploadReq.Filename)

		// Upload "multipart/form-data"
	} else if strings.HasPrefix(contentType, "multipart/form-data") {
		inMemoryFile, handler, err := r.FormFile(config.Fileparam)
		if err != nil {
			handleError(&w, fmt.Sprintf("Error looking for %s", config.Fileparam), err, http.StatusBadRequest)
			return
		}
		uploadReq.Filename, uploadReq.Origin = handler.Filename, "multipart/form-data"
		// delegate dump from request to temporary file to copyFileFromMultipart() function
		uploadReq.LocalPath, uploadReq.Size = copyFileFromMultipart(inMemoryFile, handler.Filename)

	} else { // We only support json/multipart form data content types
		handleError(&w, "can only process JSON or multipart/form-data requests", nil, http.StatusUnsupportedMediaType)
		return
	}

	if uploadReq.Size < 1 {
		handleError(&w, fmt.Sprintf("uploadRequest %v unexpected dumpsize < 1", uploadReq), nil, http.StatusBadRequest)
		return
	}
	logger.Debug().Msgf("PostObject successfully dumped to temp storage as %s", uploadReq.LocalPath)

	// Push the uploadReq onto the queue.
	uploadReq.Key = fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, uploadReq.Filename)

	// Push the uploadReq onto the queue.
	uploadQueue <- *uploadReq
	logger.Printf("S3UploadRequest %s queued with requestId=%s", uploadReq.Key, uploadReq.RequestId)

	w.Header().Set(ContentTypeKey, ContentTypeJson)
	uploadRequestJson, err := json.Marshal(uploadReq)
	if err != nil {
		handleError(&w, "cannot marshal uploadRequest", err, http.StatusInternalServerError)
		return
	}

	if _, err := w.Write(uploadRequestJson); err != nil {
		logger.Err(err).Msgf("error writing response %v", uploadRequestJson)
	}
}

// ListObjects Get a list of objects given a path such as places/12345
func ListObjects(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, _ := extractEntityVars(r)
	prefix := fmt.Sprintf("%s%s/%s", config.S3Prefix, entityType, entityId)
	lr, _ := s3Handler.ListObjectsForEntity(prefix)
	// https://stackoverflow.com/questions/28595664/how-to-stop-json-marshal-from-escaping-and/28596225
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	enc := json.NewEncoder(w)
	enc.SetEscapeHTML(false) // or & will be escaped with unicode chars
	if err := enc.Encode(&lr.Items); err != nil {
		log.Error().Err(err).Msg(err.Error())
	}
}

// GetObjectPresignUrl Get pre-signed url for  given a path such as places/12345/hase.txt
// support for resized version if ?small, ?medium and ?large request param is present
func GetObjectPresignUrl(w http.ResponseWriter, r *http.Request) {
	// check for ?small etc. request param
	resizePath := parseResizeParams(r)

	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s%s", config.S3Prefix, entityType, entityId, resizePath, item)
	target := s3Handler.GetS3PreSignedUrl(key)
	log.Printf("redirecting to key %s with presign url", key)
	http.Redirect(w, r, target,
		// see comments below and consider the codes 308, 302, or 301
		http.StatusTemporaryRedirect)
}

// DeleteObject deletes an object, to be implemented
func DeleteObject(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodDelete {
		http.Error(w, "only supported method is "+http.MethodDelete, http.StatusBadRequest)
		return
	}
	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s", config.S3Prefix, entityType, entityId, item)
	log.Printf("Delete %s yet to be implemented", key)
	w.WriteHeader(http.StatusNoContent) // send the headers with a 204 response code
}

// Health A very simple Http Health check that returns some server info (and of course http 200)
func Health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	status, err := json.Marshal(map[string]interface{}{
		"status":   "up",
		"info":     fmt.Sprintf("%s is healthy", AppId),
		"time":     time.Now().Format(time.RFC3339),
		"memstats": MemStats(),
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if _, err := w.Write(status); err != nil {
		log.Error().Msgf("[ERROR] write status %d - %v", status, err)
	}
}

/*  *** Internal Helper *** */

// downloadFile is called by PostObject if request payload is download request
func downloadFile(url string, filename string) (string, int64) {

	localFilename := filepath.Join(config.Dumpdir, filename)
	// Get the data
	resp, err := http.Get(url)
	if err != nil {
		log.Printf("%s", err)
		return "", -1
	}
	defer checkedClose(resp.Body)

	// Create the file
	out, err := os.Create(localFilename)
	if err != nil {
		log.Printf("%s", err)
		return "", -1
	}
	defer checkedClose(out)

	// Everybody ... yeah yeah ... Write the body ... yeah yeah
	_, err = io.Copy(out, resp.Body)
	if err != nil {
		log.Error().Msgf("%s", err)
		return "", -1
	}
	fSize := FileSize(out)
	return localFilename, fSize
}

// copyFileFromMultipart is called by PostObject if payload turns out to be a multipart file
// which will be dumped into a local temporary file
func copyFileFromMultipart(inMemoryFile multipart.File, filename string) (string, int64) {
	defer checkedClose(inMemoryFile)
	//fmt.Fprintf(w, "%v", handler.Header)
	localFilename := filepath.Join(config.Dumpdir, filename)
	localFile, err := os.OpenFile(localFilename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		log.Error().Msgf("Error OpenFile %s: %v", localFilename, err)
		return "", -1
	}
	defer checkedClose(localFile)
	if _, err := io.Copy(localFile, inMemoryFile); err != nil {
		log.Error().Msgf("Error copy local file %s: %v", localFile.Name(), err)
		return "", 0 // todo we should escalate this, and return error as 3rd arg
	}
	fSize := FileSize(localFile)
	return localFilename, fSize
}

// handleError logs the error and send http error response to client
func handleError(writer *http.ResponseWriter, msg string, err error, code int) {
	log.Error().Msgf("[ERROR] %s - %v", msg, err)
	http.Error(*writer, fmt.Sprintf("%s - %v", msg, err), code)
}

// extractEntityVars returns common vars from path variables e.g. /{entityType}/{entityId}/{item}"
func extractEntityVars(r *http.Request) (entityType string, entityId string, key string) {
	vars := mux.Vars(r)
	return vars["entityType"], vars["entityId"], vars["item"] // if not found -> no problem
}

// parseResizeParams checks for ?small, ?medium etc. request parameters
func parseResizeParams(r *http.Request) string {
	parseErr := r.ParseForm()
	if parseErr != nil {
		log.Printf("WARN: Cannot parse request URI %s", r.RequestURI)
		return ""
	}
	for resizeMode := range config.ResizeModes {
		_, isRequested := r.Form[resizeMode]
		if isRequested {
			return resizeMode + "/"
		}
	}
	return ""
}
