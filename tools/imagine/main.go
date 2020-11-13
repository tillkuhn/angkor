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
	S3Prefix      string        `default:"appdata/"`
	PresignExpiry time.Duration `default:"30m"` // e.g. HEALTHBELLS_INTERVAL=5s
	Dumpdir       string        `default:"./upload"`
	Fileparam     string        `default:"uploadfile"`
	Port          int           `default:"8090"`
	Queuesize     int           `default:"10"`
	Timeout       time.Duration `default:"20s"` // e.g. HEALTHBELLS_INTERVAL=5s
	Contextpath   string        `default:""`
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
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", redirectPresignUrl).Methods("GET")
	// all objects as json list
	router.HandleFunc(cp+"/{entityType}/{entityId}", objectList).Methods("GET")
	// upload
	router.HandleFunc(cp+"/{entityType}/{entityId}", uploadObject).Methods("POST")
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
