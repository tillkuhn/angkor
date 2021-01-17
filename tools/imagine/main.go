package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"path"
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
	EnableAuth    bool           `default:"true" split_words:"true" desc:"Enabled basic auth checking for post and delete requests"`
	ForceGc       bool           `default:"false" split_words:"true" desc:"For systems low on memory, force gc/free memory after mem intensive ops"`
}

var (
	uploadQueue chan UploadRequest
	s3Handler   S3Handler
	config      Config
	BuildTime   string = ""
)

func main() {
	fmt.Printf("%s build %s starting ...\n",path.Base(os.Args[0]),BuildTime)
	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
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

	// Health info
	router.HandleFunc(cp+"/health", Health).Methods(http.MethodGet)

	// Redirect to presign url for a particular file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", GetObjectPresignUrl).Methods(http.MethodGet)

	// Delete file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", DeleteObject).Methods(http.MethodDelete)

	// All objects as json list
	router.HandleFunc(cp+"/{entityType}/{entityId}", ListObjects).Methods(http.MethodGet)

	// Upload new file multipart
	router.HandleFunc(cp+"/{entityType}/{entityId}", PostObject).Methods(http.MethodPost)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static, running only as API Server ")
	} else {
		log.Printf("Setting up route to local /static directory")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}
	dumpRoutes(router) // show all routes

	// Configure AWS Session which will be reused by S3 Upload Worker
	log.Printf("Establish AWS Session target bucket=%s prefix=%s", config.S3Bucket, config.S3Prefix)
	awsSession, errAWS := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if errAWS != nil {
		log.Fatalf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler = S3Handler{
		Session: awsSession,
	}

	// Start worker queue goroutine with buffered Queue
	uploadQueue = make(chan UploadRequest, config.QueueSize)
	log.Printf("Starting S3 Upload Worker queue with bufsize=%d", config.QueueSize)
	go s3Handler.StartWorker(uploadQueue)

	// Launch the HTTP Server and block
	log.Printf("Start HTTPServer http://localhost:%d%s", config.Port, config.Contextpath)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d", config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}
	log.Fatal(srv.ListenAndServe())
}

// Helper function to show each route + method, https://github.com/gorilla/mux/issues/186
func dumpRoutes(r *mux.Router) {
	r.Walk(func(route *mux.Route, router *mux.Router, ancestors []*mux.Route) error {
		t, _ := route.GetPathTemplate()
		m, _ := route.GetMethods()
		log.Printf("Registered route: %v %s", m, t)
		return nil
	})
}
