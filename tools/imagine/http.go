package main

// https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"sync"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/gorilla/mux"
)

const UPLOADIR = "./upload/"

// Singleton pattern http://marcio.io/2015/07/singleton-pattern-in-go/
var (
	s3Handler S3Handler
	once      sync.Once
)

// upload logic
func uploadToTmp(w http.ResponseWriter, r *http.Request) {
	once.Do(func() {
		//psqlInfo := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
		log.Printf("Establish AWS Session")
		sess, errAWS := session.NewSession(&aws.Config{Region: aws.String(S3_REGION)})
		if errAWS != nil {
			log.Fatalf("session.NewSession (AWS) err: %v", errAWS)
		}
		s3Handler = S3Handler{
			Session: sess,
			Bucket:  S3_BUCKET,
		}
	})

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
	log.Printf("Uploaded %s to temp storage %s", handler.Filename, localFilename)
	uploadToS3(localFilename)

	thumbnail := createThumbnail(localFilename)
	uploadToS3(thumbnail)

}

func uploadToS3(localFilename string) {
	err := s3Handler.UploadFile(filepath.Join("hase", localFilename), localFilename)
	if err != nil {
		log.Fatalf("UploadFile - filename: %v, err: %v", localFilename, err)
	}
	log.Printf("UploadFile - success")

}
func apiGet(w http.ResponseWriter, r *http.Request) {
	json.NewEncoder(w).Encode(" API endpoint ")
}

func HealthCheckHandler(w http.ResponseWriter, r *http.Request) {
	// A very simple health check.
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	// In the future we could report back on the status of our DB, or our cache
	// (e.g. Redis) by performing a simple PING, and include them in the response.
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
