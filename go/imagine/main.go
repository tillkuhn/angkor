package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"runtime"
	"syscall"
	"time"

	"github.com/rs/zerolog"

	"github.com/tillkuhn/angkor/tools/imagine/auth"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"
	"github.com/rs/zerolog/log"
	"github.com/tillkuhn/angkor/tools/topkapi"
)

// Config usage is displayed when called with -h
// IMAGINE_JWKS_ENDPOINT
type Config struct {
	AWSRegion     string         `default:"eu-central-1" required:"true" desc:"AWS Region"`
	ContextPath   string         `default:"" desc:"optional context path for http server e.g. /api" split_words:"false"`
	Debug         bool           `default:"false" desc:"debug mode for more verbose output"`
	Dumpdir       string         `default:"./upload" desc:"temporary local upload directory"`
	EnableAuth    bool           `default:"true" split_words:"true" desc:"Enabled basic auth checking for post and delete requests"`
	Fileparam     string         `default:"uploadfile" desc:"name of the param that holds the file in multipart request"`
	ForceGc       bool           `default:"false" split_words:"true" desc:"For systems low on memory, force gc/free memory after mem intensive ops"`
	JwksEndpoint  string         `split_words:"true" desc:"Endpoint to download JWKS"`
	KafkaSupport  bool           `default:"true" desc:"Send important events to Kafka Topic(s)" split_words:"true"`
	KafkaTopic    string         `default:"app" desc:"Default Kafka Topic for published Events" split_words:"true"`
	Port          int            `default:"8090" desc:"http server port"`
	PresignExpiry time.Duration  `default:"30m" desc:"how long presigned urls are valid"`
	QueueSize     int            `default:"10" split_words:"true" desc:"max capacity of s3 upload worker queue"`
	ResizeModes   map[string]int `default:"small:150,medium:300,large:600" split_words:"true" desc:"map modes with width"`
	ResizeQuality int            `default:"80" split_words:"true" desc:"JPEG quality for resize"`
	S3Bucket      string         `required:"true" desc:"Name of the S3 Bucket w/o s3:// prefix"`
	S3Prefix      string         `default:"imagine/" desc:"key prefix, leave empty to use bucket root"`
	Timeout       time.Duration  `default:"30s" desc:"Duration until http server times out"`
}

var (
	uploadQueue chan UploadRequest
	s3Handler   S3Handler
	jwtAuth     *auth.JwtAuth
	config      Config
	// BuildTime will be overwritten by ldflags during build, e.g. -X 'main.BuildTime=2021...
	BuildTime = "latest"
	AppId     = "imagine"
)

func main() {
	// Configure logging
	zerolog.SetGlobalLevel(zerolog.DebugLevel)
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr, TimeFormat: "Jan-02 15:04:05"}).With().Str("app", AppId).Logger()
	mainLogger := log.Logger.With().Str("logger", "main").Logger()

	// Catch HUP and INT signals https://gist.github.com/reiki4040/be3705f307d3cd136e85
	mainLogger.Info().Msgf("Setting up signal handler for %d", syscall.SIGHUP)
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, syscall.SIGHUP, syscall.SIGINT)
	go func() {
		for {
			s := <-signalChan
			switch s {
			// kill -SIGHUP pid
			case syscall.SIGHUP:
				mainLogger.Info().Msgf("Received hangover signal (%v), let's do something", s)
			// kill -SIGINT pid
			case syscall.SIGINT:
				mainLogger.Info().Msgf("Received SIGINT (%v), terminating", s)
				os.Exit(2)
			default:
				mainLogger.Warn().Msgf("ignoring unexpected signal %d", s)
			}
		}
	}()

	startMsg := fmt.Sprintf("Starting service [%s] build=%s PID=%d OS=%s LogLeve=%s",
		AppId, BuildTime, os.Getpid(), runtime.GOOS, zerolog.GlobalLevel().String())
	mainLogger.Info().Msg(startMsg)

	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		if err := envconfig.Usage(AppId, &config); err != nil {
			mainLogger.Printf(err.Error())
		}
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(AppId, &config)
	if err != nil {
		mainLogger.Fatal().Msg(err.Error())
	}

	// Kafka send start event to topic
	kafkaClient := topkapi.NewClientWithId(AppId)
	defer kafkaClient.Close()
	kafkaClient.Enable(config.KafkaSupport)
	if _, _, err := kafkaClient.PublishEvent(kafkaClient.NewEvent("startup:"+AppId, startMsg), "system"); err != nil {
		mainLogger.Error().Msgf("Error publish event to %s: %v", "system", err)
	}

	// Configure HTTP Router`
	cp := config.ContextPath
	router := mux.NewRouter()

	// Health info
	router.HandleFunc(cp+"/health", Health).Methods(http.MethodGet)

	// Redirect to presigned url for a particular file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", GetObjectPresignUrl).Methods(http.MethodGet)

	// Delete file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", DeleteObject).Methods(http.MethodDelete)

	// All objects as json list
	router.HandleFunc(cp+"/{entityType}/{entityId}", ListObjects).Methods(http.MethodGet)

	// Upload new file multipart
	router.HandleFunc(cp+"/{entityType}/{entityId}", PostObject).Methods(http.MethodPost)

	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		mainLogger.Printf("No Static dir /static, running only as API Server ")
	} else {
		mainLogger.Printf("Setting up route to local /static directory")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}
	dumpRoutes(router) // show all routes

	// Configure AWS Session which will be reused by S3 Upload Worker
	mainLogger.Info().Msgf("Establish AWS Session target bucket=%s prefix=%s", config.S3Bucket, config.S3Prefix)
	awsSession, errAWS := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if errAWS != nil {
		mainLogger.Fatal().Msgf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler = S3Handler{
		Session:   awsSession,
		Publisher: kafkaClient,
		log:       log.Logger.With().Str("logger", "s3worker").Logger(),
	}

	// Start S3 Upload Worker Queue goroutine with buffered Queue
	uploadQueue = make(chan UploadRequest, config.QueueSize)
	mainLogger.Printf("Starting S3 Upload Worker queue with bufsize=%d", config.QueueSize)
	go s3Handler.StartWorker(uploadQueue)

	// If auth is enabled, init JWKS
	if config.EnableAuth {
		jwtAuth, err = auth.NewJwtAuth(config.JwksEndpoint)
		if err != nil {
			mainLogger.Fatal().Err(err).Msg(err.Error())
		}
	}

	// Launch the HTTP Server and block
	mainLogger.Info().Msgf("Start HTTPServer http://localhost:%d%s", config.Port, config.ContextPath)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d", config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}
	mainLogger.Fatal().Err(srv.ListenAndServe())
}

// Helper function to show each route + method, https://github.com/gorilla/mux/issues/186
func dumpRoutes(r *mux.Router) {
	if err := r.Walk(func(route *mux.Route, router *mux.Router, ancestors []*mux.Route) error {
		t, _ := route.GetPathTemplate()
		m, _ := route.GetMethods()
		log.Printf("Registered route: %v %s", m, t)
		return nil
	}); err != nil {
		log.Printf(err.Error())
	}
}
