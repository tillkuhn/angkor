package main

// https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
// https://nesv.github.io/golang/2014/02/25/worker-queues-in-go.html
import (
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"io"
	"log"
	"net/http"
	"os"
)

const UPLOADIR = "./upload/"

// Singleton pattern http://marcio.io/2015/07/singleton-pattern-in-go/

// receive file from http request, dump to local storage first
func uploadToTmp(w http.ResponseWriter, r *http.Request) {
	// init aws session, but only once ...

	log.Printf("method: %v path: %v", r.Method, r.URL.Path)
	vars := mux.Vars(r)
	log.Printf("entityType: %v id %v\n", vars["entityType"], vars["entityId"])
	r.ParseMultipartForm(32 << 20)
	uploadFile, handler, err := r.FormFile("uploadfile")
	if err != nil {
		fmt.Println(err)
		return
	}
	defer uploadFile.Close()
	fmt.Fprintf(w, "%v", handler.Header)
	localFilename := UPLOADIR + handler.Filename
	f, err := os.OpenFile(localFilename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		fmt.Println(err)
		return
	}
	defer f.Close()
	io.Copy(f, uploadFile)
	log.Printf("Uploaded %s dumped to temp storage %s", handler.Filename, localFilename)
	// Push the work onto the queue.
	// Now, we take the delay, and the person's name, and make a WorkRequest out of them.
	work := WorkRequest{Name: localFilename}

	// Push the work onto the queue.
	jobChan <- work
	log.Printf("Work request queued for %s",localFilename)

	// And let the user know their work request was created.
	// w.WriteHeader(http.StatusCreated)
	//uploadToS3(localFilename)

	// create tumbnail from image and also upload
	//thumbnail := createThumbnail(localFilename)
	//uploadToS3(thumbnail)

}


func apiGet(w http.ResponseWriter, r *http.Request) {
	json.NewEncoder(w).Encode(" API endpoint ")
}

// A very simple health check.
func HealthCheckHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	io.WriteString(w, `{"alive": true}`)
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
