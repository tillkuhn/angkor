package main

import (
	"flag"
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

// usage is displayed when called with -h
type Config struct {
	AWSRegion     string         `default:"eu-central-1" required:"true" desc:"AWS Region"`
	S3Bucket      string         `required:"true" desc:"Name of the S3 Bucket w/o s3://"`
	S3Prefix      string         `default:"imagine/" desc:"key prefix, leave empty to use bucket root"`
	Contextpath   string         `default:"" desc:"optional context path for http server"`
	PresignExpiry time.Duration  `default:"30m" desc:"how long presign urls are valid"`
	Dumpdir       string         `default:"./upload" desc:"temporary local upload directory"`
	Fileparam     string         `default:"uploadfile" desc:"name of param in multipart request"`
	Port          int            `default:"8090" desc:"server http port"`
	QueueSize     int            `default:"10" split_words:"true" desc:"maxlen of s3 upload queue"`
	ResizeQuality int            `default:"80" split_words:"true" desc:"JPEG quality for resize"`
	ResizeModes   map[string]int `default:"small:150,medium:300,large:600" split_words:"true" desc:"map modes with width"`
	Timeout       time.Duration  `default:"30s" desc:"http server timeouts"`
	Debug         bool           `default:"false" desc:"debug mode for more verbose output"`
}

var (
	uploadQueue chan UploadRequest
	s3Handler   S3Handler
	config      Config
)

func main() {
	var help = flag.Bool("h", false, "display help message")
	flag.Parse()
	if *help {
		envconfig.Usage(appPrefix, &config)
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatal(err.Error())
	}

	// Configure HTTP Router`
	cp := config.Contextpath
	router := mux.NewRouter()

	// redirect to presign url for a particular file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", GetObjectPresignUrl).Methods(http.MethodGet)

	// delete file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", DeleteObject).Methods(http.MethodDelete)

	// all objects as json list
	router.HandleFunc(cp+"/{entityType}/{entityId}", ListObjects).Methods(http.MethodGet)

	// upload new file multipart
	router.HandleFunc(cp+"/{entityType}/{entityId}", PostObject).Methods(http.MethodPost)

	// Health info
	router.HandleFunc(cp+"/Health", Health)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static, running only as API Server ")
	} else {
		log.Printf("Setting up route to local /static directory")
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
	uploadQueue = make(chan UploadRequest, config.QueueSize)
	log.Printf("Starting S3 Upload Worker queue with bufsize=%d", config.QueueSize)
	go s3Handler.StartWorker(uploadQueue)

	log.Printf("Start HTTPServer http://localhost:%d%s", config.Port, config.Contextpath)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d", config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}

	log.Fatal(srv.ListenAndServe())
}
