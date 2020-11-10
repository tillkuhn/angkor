package main

import (
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/gorilla/mux"
)

const (
	S3_REGION = "eu-central-1"
	S3_BUCKET = "timafe-angkor-data-dev"
	PORT      = "8090"
	// S3_ACL    = "public-read"
)
type WorkRequest struct {
	Name  string
	// Delay time.Duration
}
var (
	jobChan = make(chan WorkRequest, 3)
	s3Handler S3Handler
)

func main() {
	router := mux.NewRouter()
	router.HandleFunc("/upload/{entityType}/{entityId}", uploadToTmp).Methods("POST")
	router.HandleFunc("/api", apiGet).Methods("GET")
	router.HandleFunc("/health", HealthCheckHandler)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static")
	} else {
		log.Printf("Setting up route to local /static")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}

	// Start the dispatcher.
	log.Printf("Establish AWS Session")
	sess, errAWS := session.NewSession(&aws.Config{Region: aws.String(S3_REGION)})
	if errAWS != nil {
		log.Fatalf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler = S3Handler{
		Session: sess,
		Bucket:  S3_BUCKET,
	}

	log.Printf("Starting the worker")
	go worker(jobChan)

	log.Printf("HTTP Server Listen on http://localhost:%s", PORT)
	srv := &http.Server{
		Handler:      router,
		Addr:         ":" + PORT,
		WriteTimeout: 15 * time.Second,
		ReadTimeout:  15 * time.Second,
	}

	log.Fatal(srv.ListenAndServe())
}

func worker(jobChan <-chan WorkRequest) {
	for job := range jobChan {
		log.Printf("process %v",job)
		uploadToS3(job.Name)
	}
}

func uploadToS3(localFilename string) {
	err := s3Handler.UploadFile(filepath.Join("hase", localFilename), localFilename)
	if err != nil {
		log.Fatalf("UploadFile - filename: %v, err: %v", localFilename, err)
	}
	log.Printf("UploadFile - success")
}
