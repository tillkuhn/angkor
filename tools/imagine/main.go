package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "imagine"

type Config struct {
	AWSRegion string `default:"eu-central-1"`
	S3Bucket  string `default:"timafe-angkor-data-dev"`
	S3Prefix  string `default:"appdata"`
	Dumpdir   string `default:"./upload"`
	Fileparam string `default:"uploadfile"`
	Port      int    `default:"8090"`
	Queuesize int    `default:"10"`
	Timeout time.Duration `default:"20s"` // e.g. HEALTHBELLS_INTERVAL=5s
}

var (
	uploadQueue chan UploadRequest
	s3Handler   S3Handler
	config Config
)

func main() {
	// Parse config
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatal(err.Error())
	}
	
	// Configure HTTP Router
	router := mux.NewRouter()
	router.HandleFunc("/upload/{entityType}/{entityId}", uploadToTmp).Methods("POST")
	router.HandleFunc("/api", apiGet).Methods("GET")
	router.HandleFunc("/health", health)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static")
	} else {
		log.Printf("Setting up route to local /static")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}

	// Configure AWS
	log.Printf("Establish AWS Session target bucket=%s prefix=%s",config.S3Bucket,config.S3Prefix)
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
	uploadQueue = make(chan UploadRequest, config.Queuesize)
	log.Printf("Starting worker queue with buffersize %d",config.Queuesize)
	go worker(uploadQueue)

	log.Printf("Start HTTP http://localhost:%d with timeout %v", config.Port,config.Timeout)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d",config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}

	log.Fatal(srv.ListenAndServe())
}

