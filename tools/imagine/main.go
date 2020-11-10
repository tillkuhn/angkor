package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"

)

const appPrefix = "imagine"

type Config struct {
	AWSRegion string `default:"eu-central-1"`
	S3Bucket  string `default:"timafe-angkor-data-dev"`
	S3Prefix  string `default:"appdata/"`
	Port      int    `default:"8090"`
	Queuesize int    `default:"10"`
	Timeout time.Duration `default:"20s"` // e.g. HEALTHBELLS_INTERVAL=5s
}

type WorkRequest struct {
	Name  string
	RequestId string
	// Delay time.Duration
}
var (
	jobChan chan WorkRequest
	s3Handler S3Handler
)

func main() {
	// Parse config
	var config Config
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatal(err.Error())
	}
	
	// Configure HTTP Router
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

	// Configure AWS
	log.Printf("Establish AWS Session")
	sess, errAWS := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if errAWS != nil {
		log.Fatalf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler = S3Handler{
		Session: sess,
		Bucket:  config.S3Bucket,
		KeyPrefix: config.S3Prefix,
	}

	// Start worker queue goroutine
	jobChan= make(chan WorkRequest, config.Queuesize)
	log.Printf("Starting worker queue with buffersize %d",config.Queuesize)
	go worker(jobChan)

	log.Printf("Start HTTP http://localhost:%d with timeout %v", config.Port,config.Timeout)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d",config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
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
