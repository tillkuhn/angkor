package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "imagine"

type Config struct {
	AWSRegion     string        `default:"eu-central-1"`
	S3Bucket      string        `default:"timafe-angkor-data-dev"`
	S3Prefix      string        `default:"imagine/"`  // key prefix, leave empty to use bucket root
	Contextpath   string        `default:""`  // optional context path for http server, default is root
	PresignExpiry time.Duration `default:"30m"` //
	Dumpdir       string        `default:"./upload"`  // temporary local upload directory
	Fileparam     string        `default:"uploadfile"`
	Port          int           `default:"8090"`
	Queuesize     int           `default:"10"`
	Thumbsize     int           `default:"100"`
	Thumbquality  int           `default:"80"`
	Thumbsubdir   string         `default:"thumbs"`
	Timeout       time.Duration `default:"20s"`
}

var (
	uploadQueue chan UploadRequest
	s3Handler   S3Handler
	config      Config
)

func main() {
	// Parse config
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatal(err.Error())
	}

	// Configure HTTP Router`
	cp := config.Contextpath
	router := mux.NewRouter()
	// redirect to presign url
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", GetObjectPresignUrl).Methods("GET")
	router.HandleFunc(cp+"/{entityType}/{entityId}/"+config.Thumbsubdir+"/{item}", GetObjectThumbnailPresignUrl).Methods("GET")
	// all objects as json list
	router.HandleFunc(cp+"/{entityType}/{entityId}", ListObjects).Methods("GET")
	// upload
	router.HandleFunc(cp+"/{entityType}/{entityId}", UploadObject).Methods("POST")
	// router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", presignUrl).Methods("GET")
	// health info
	router.HandleFunc(cp+"/health", health)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static")
	} else {
		log.Printf("Setting up route to local /static")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}

	// Configure AWS
	log.Printf("Establish AWS Session target bucket=%s prefix=%s", config.S3Bucket, config.S3Prefix)
	sess, errAWS := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if errAWS != nil {
		log.Fatalf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler = S3Handler{
		Session: sess,
	}

	// Start worker queue goroutine
	uploadQueue = make(chan UploadRequest, config.Queuesize)
	log.Printf("Starting worker queue with buffersize %d", config.Queuesize)
	go s3Handler.StartWorker(uploadQueue)

	log.Printf("Start HTTP http://localhost:%d%s", config.Port, config.Contextpath)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d", config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}

	log.Fatal(srv.ListenAndServe())
}
