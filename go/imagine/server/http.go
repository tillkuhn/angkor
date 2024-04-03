package server

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

	"github.com/jellydator/ttlcache/v3"

	"github.com/rs/zerolog"
	"github.com/tillkuhn/angkor/tools/imagine/s3"
	"github.com/tillkuhn/angkor/tools/imagine/types"
	"github.com/tillkuhn/angkor/tools/imagine/utils"

	"github.com/gorilla/context"
	"github.com/tillkuhn/angkor/tools/imagine/auth"

	"github.com/rs/zerolog/log"

	"github.com/gorilla/mux"
	"github.com/rs/xid"
)

const (
	ContentTypeJson       = "application/json"
	ContentTypeKey        = "Content-Type"
	AliasAllFilesInFolder = "_all"
)

type Handler struct {
	config    *types.Config
	log       zerolog.Logger
	s3Handler *s3.Handler
	// urlCache caches presign urls, so they can be re-used as long as they are still valid
	urlCache *ttlcache.Cache[string, string]
}

// NewHandler configures / instantiates a new HTTP Handler with presign url cache
func NewHandler(s3Handler *s3.Handler, config *types.Config) *Handler {
	cache := ttlcache.New[string, string](
		// cache expiry date should be prior to the actual expiry of the URL
		// so the urls can be still used for some time
		// if config.PresignExpiry is < 5m, objects will be (probably) removed immediately on the first cleanup run
		ttlcache.WithTTL[string, string](config.PresignExpiry-(5*time.Minute)),
		// this is important, or the expiration will be prolonged whenever Get is successfully invoked on a key
		ttlcache.WithDisableTouchOnHit[string, string](),
	)
	log.Printf("Init HTTP Handler, starting url cache cleanup routine")
	go cache.Start() // start auto cleanup in separate goroutine
	return &Handler{
		s3Handler: s3Handler,
		config:    config,
		urlCache:  cache,
		log:       log.Logger.With().Str("logger", "server").Logger(),
	}
}

func (h *Handler) Close() {
	if h.urlCache != nil {
		log.Printf("Closing HTTP Handler, stopping url cache")
		h.urlCache.Stop()
	}
}

// PostSong Dedicated Upload Handler for Songs such as mp3 files
func (h *Handler) PostSong(w http.ResponseWriter, r *http.Request) {
	// overwrite mux vars
	vars := mux.Vars(r)
	vars["entityType"] = "songs"
	vars["entityId"] = ""
	h.PostObject(w, r) // Delegate to standard Post
}

// ListSongs Dedicated List Handler for Songs such as mp3 files e.g. /songs/{folder}/
func (h *Handler) ListSongs(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	vars["entityType"] = "songs"
	vars["entityId"] = vars["folder"]
	if vars["folder"] == strings.TrimSuffix(AliasAllFilesInFolder, "/") {
		h.log.Debug().Msgf("Alias %s translated to all songs in folder", vars["folder"])
		vars["entityId"] = ""
	}
	h.ListObjects(w, r) // Delegate to standard List
}

// ListFolders Returns S3 common prefixes for imagine/{rootFolder} toplevel folders
func (h *Handler) ListFolders(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	folderPath := fmt.Sprintf("%s%s/", h.config.S3Prefix, vars["rootFolder"])
	log.Debug().Msgf("Listing sub-folders in %s", folderPath)
	resp, err := h.s3Handler.ListFolders(folderPath)
	if err != nil {
		http.Error(w, fmt.Sprintf("error list folders %s", err.Error()), http.StatusInternalServerError)
		return
	}
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	jsonResp, err := json.Marshal(resp)
	if err != nil {
		http.Error(w, fmt.Sprintf("error marshal list response %s", err.Error()), http.StatusInternalServerError)
		return
	}
	if _, err := w.Write(jsonResp); err != nil {
		log.Error().Msgf("[ERROR] write jsonResp %d - %v", jsonResp, err)
	}

}

// PostObject extract file from http request (json or multipart)
// dumps it to local storage first, and creates a job for s3 upload
//
// File Upload with Golang: https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
// Worker queues in Go: https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html
//
// URL Pattern: cp+"/{entityType}/{entityId}"
func (h *Handler) PostObject(w http.ResponseWriter, r *http.Request) {
	logger := log.Logger.With().Str("logger", "http").Logger()
	entityType, entityId, _ := extractEntityVars(r)
	uploadReq := &types.UploadRequest{RequestId: xid.New().String(), EntityId: entityId}

	// Looks also promising: https://golang.org/pkg/net/http/#DetectContentType DetectContentType
	// implements the algorithm described at https://mimesniff.spec.whatwg.org/ to determine the Content-Type of the given data.
	contentType := r.Header.Get(ContentTypeKey) /* case-insensitive, returns "" if not found */
	authInfo := context.Get(r, auth.ContextAuthKey).(*auth.TokenInfo)
	logger.Debug().Msgf("PostObject requestId=%s path=%v entityType=%v id=%v authScope=%v",
		uploadReq.RequestId, r.URL.Path, entityType, entityId, authInfo.Scope())

	// distinguish JSON formatted download request and "multipart/form-data"
	if strings.HasPrefix(contentType, ContentTypeJson) {
		decoder := json.NewDecoder(r.Body)
		var dr types.DownloadRequest
		err := decoder.Decode(&dr) // check if request can be parsed into JSON
		if err != nil {
			http.Error(w, fmt.Sprintf("Cannot parse %v into DownloadRequest", r.Body), http.StatusBadRequest)
			return
		}
		if dr.URL == "" {
			http.Error(w, fmt.Sprintf("key 'url' not found in DownloadRequest %v", dr), http.StatusBadRequest)
			return
		}
		uploadReq.Origin = dr.URL
		// if request contains a filename, use this instead of url filename but append suffix if not present
		fileExtension := strings.ToLower(utils.StripRequestParams(filepath.Ext(dr.URL))) // .JPG -> .jpg
		if dr.Filename != "" {
			uploadReq.Filename = dr.Filename
			if !utils.HasExtension(uploadReq.Filename) {
				uploadReq.Filename = uploadReq.Filename + fileExtension
				// if the original URL also has no extension, we have to rely on s3 worker
				// which detected the Mimetype to fix this
			}
		} else {
			uploadReq.Filename = utils.StripRequestParams(path.Base(dr.URL))
		}
		logger.Printf("Trigger URL DownloadRequest url=%s filename=%s ext=%s", dr.URL, uploadReq.Filename, fileExtension)
		// delegate actual download from URL to downloadFile
		uploadReq.LocalPath, uploadReq.Size = h.downloadFile(dr.URL, uploadReq.Filename)

		// Upload "multipart/form-data"
	} else if strings.HasPrefix(contentType, "multipart/form-data") {
		inMemoryFile, handler, err := r.FormFile(h.config.Fileparam)
		if err != nil {
			http.Error(w, fmt.Sprintf("Error looking for %s", h.config.Fileparam), http.StatusBadRequest)
			return
		}
		uploadReq.Filename, uploadReq.Origin = handler.Filename, "multipart/form-data"
		// delegate dump from request to temporary file to copyFileFromMultipart() function
		uploadReq.LocalPath, uploadReq.Size = h.copyFileFromMultipart(inMemoryFile, handler.Filename)

	} else { // We only support json/multipart form data content types
		http.Error(w, "can only process JSON or multipart/form-data requests", http.StatusUnsupportedMediaType)
		return
	}

	if uploadReq.Size < 1 {
		http.Error(w, fmt.Sprintf("uploadRequest %v unexpected dumpsize < 1", uploadReq), http.StatusBadRequest)
		return
	}
	logger.Debug().Msgf("PostObject successfully dumped to temp storage as %s", uploadReq.LocalPath)

	// Push the uploadReq onto the queue.
	uploadReq.Key = fmt.Sprintf("%s%s/%s/%s", h.config.S3Prefix, entityType, entityId, uploadReq.Filename)

	// Push the uploadReq onto the queue.
	h.s3Handler.UploadRequest(uploadReq)
	logger.Printf("S3UploadRequest %s queued with requestId=%s", uploadReq.Key, uploadReq.RequestId)

	w.Header().Set(ContentTypeKey, ContentTypeJson)
	uploadRequestJson, err := json.Marshal(uploadReq)
	if err != nil {
		http.Error(w, fmt.Sprintf("cannot marshal uploadRequest %s", err.Error()), http.StatusInternalServerError)
		return
	}

	if _, err := w.Write(uploadRequestJson); err != nil {
		logger.Err(err).Msgf("error writing response %v", uploadRequestJson)
	}
}

// GetObjectPresignUrl Get pre-signed url for  given a path such as places/12345/hase.txt
// support for resized version if ?small, ?medium and ?large request param is present
//
// NEW: Support cached URLs to enable browsers to cache identical image with different aws s3 presigned url
// @see https://github.com/tillkuhn/angkor/issues/226
//
// HTTP/1.1 307 Temporary Redirect
// Cache-Control: max-age=1800
// Location: https://<bucket>.s3.eu-central-1.amazonaws.com/imagine/places/4711/hase2.jpeg?X-Amz-Algorithm=AWS4-(...)
func (h *Handler) GetObjectPresignUrl(w http.ResponseWriter, r *http.Request) {
	// check for ?small etc. request param
	resizePath := h.parseResizeParams(r)

	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s%s", h.config.S3Prefix, entityType, entityId, resizePath, item)
	var target string
	// log.Printf("Looking for %s", key)
	if h.urlCache != nil && h.urlCache.Has(key) {
		cItem := h.urlCache.Get(key)
		log.Printf("Reusing cached presignURL %s, still valid for %v", key, cItem.ExpiresAt().Sub(time.Now()).Round(time.Second))
		target = cItem.Value()
	} else {
		target = h.s3Handler.GetS3PreSignedUrl(key)
		if h.urlCache != nil {
			h.urlCache.Set(key, target, ttlcache.DefaultTTL)
			log.Printf("Generated new cacheable presign url for %s ", key)
		} else {
			log.Printf("Generated new one-tiume presign url for %s", key)
		}
	}
	// log.Printf("redirecting to key %s with presign url", key)
	w.Header().Set("Cache-Control", fmt.Sprintf("max-age=%v", h.config.PresignExpiry.Seconds()))
	// see comments below and consider the codes 308, 302, 301 or 307 (TemporaryRedirect)
	http.Redirect(w, r, target, http.StatusTemporaryRedirect)
}

// GetSongPresignUrl Returns S3 Download Link for Song
func (h *Handler) GetSongPresignUrl(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	item := vars["item"]
	folder := vars["folder"]
	var key string
	if folder != "" {
		key = fmt.Sprintf("%s%s/%s/%s", h.config.S3Prefix, "songs", folder, item)
	} else {
		key = fmt.Sprintf("%s%s/%s", h.config.S3Prefix, "songs", item)
	}
	url := h.s3Handler.GetS3PreSignedUrl(key)
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

// ListObjects Get a list of objects given a path such as places/12345
func (h *Handler) ListObjects(w http.ResponseWriter, r *http.Request) {
	entityType, entityId, _ := extractEntityVars(r)
	prefix := fmt.Sprintf("%s%s/%s", h.config.S3Prefix, entityType, entityId)
	lr, _ := h.s3Handler.ListObjectsForEntity(prefix)
	// https://stackoverflow.com/questions/28595664/how-to-stop-json-marshal-from-escaping-and/28596225
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	enc := json.NewEncoder(w)
	enc.SetEscapeHTML(false) // or & will be escaped with unicode chars
	if err := enc.Encode(&lr.Items); err != nil {
		log.Err(err).Msg(err.Error())
	}
}

// DeleteObject deletes an object, to be implemented
func (h *Handler) DeleteObject(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodDelete {
		http.Error(w, "only supported method is "+http.MethodDelete, http.StatusBadRequest)
		return
	}
	entityType, entityId, item := extractEntityVars(r)
	key := fmt.Sprintf("%s%s/%s/%s", h.config.S3Prefix, entityType, entityId, item)
	log.Printf("Delete %s yet to be implemented", key)
	w.WriteHeader(http.StatusNoContent) // send the headers with a 204 response code
}

// Health A very simple Http Health check that returns some server info (and of course http 200)
func Health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set(ContentTypeKey, ContentTypeJson)
	status, err := json.Marshal(map[string]interface{}{
		"status":   "up",
		"info":     "I am healthy",
		"time":     time.Now().Format(time.RFC3339),
		"memstats": utils.MemStats(),
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if _, err := w.Write(status); err != nil {
		log.Err(err).Msgf("[ERROR] write status %d - %v", status, err)
	}
}

/*  *** Internal Helper *** */

// downloadFile is called by PostObject if request payload is download request
func (h *Handler) downloadFile(url string, filename string) (string, int64) {

	localFilename := filepath.Join(h.config.Dumpdir, filename)
	// Get the data
	resp, err := http.Get(url)
	if err != nil {
		log.Err(err).Msgf("cannot download url %s: %v", url, err)
		return "", -1
	}
	defer utils.CheckedClose(resp.Body)

	// Create the file
	out, err := os.Create(localFilename)
	if err != nil {
		log.Err(err).Msgf("cannot create %s: %v", localFilename, err)
		return "", -1
	}
	defer utils.CheckedClose(out)

	// Everybody ... yeah yeah ... Write the body ... yeah yeah
	_, err = io.Copy(out, resp.Body)
	if err != nil {
		log.Error().Msgf(err.Error())
		return "", -1
	}
	fSize := utils.FileSize(out)
	return localFilename, fSize
}

// copyFileFromMultipart is called by PostObject if payload turns out to be a multipart file
// which will be dumped into a local temporary file
func (h *Handler) copyFileFromMultipart(inMemoryFile multipart.File, filename string) (string, int64) {
	defer utils.CheckedClose(inMemoryFile)
	//fmt.Fprintf(w, "%v", handler.Header)
	localFilename := filepath.Join(h.config.Dumpdir, filename)
	localFile, err := os.OpenFile(localFilename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		log.Error().Msgf("Error OpenFile %s: %v", localFilename, err)
		return "", -1
	}
	defer utils.CheckedClose(localFile)
	if _, err := io.Copy(localFile, inMemoryFile); err != nil {
		log.Error().Msgf("Error copy local file %s: %v", localFile.Name(), err)
		return "", 0 // todo we should escalate this, and return error as 3rd arg
	}
	fSize := utils.FileSize(localFile)
	return localFilename, fSize
}

// extractEntityVars returns common vars from path variables e.g. /{entityType}/{entityId}/{item}"
func extractEntityVars(r *http.Request) (entityType string, entityId string, key string) {
	vars := mux.Vars(r)
	return vars["entityType"], vars["entityId"], vars["item"] // if not found -> no problem
}

// parseResizeParams checks for ?small, ?medium etc. request parameters
func (h *Handler) parseResizeParams(r *http.Request) string {
	parseErr := r.ParseForm()
	if parseErr != nil {
		log.Printf("WARN: Cannot parse request URI %s", r.RequestURI)
		return ""
	}
	for resizeMode := range h.config.ResizeModes {
		_, isRequested := r.Form[resizeMode]
		if isRequested {
			return resizeMode + "/"
		}
	}
	return ""
}
