package main

import (
	"flag"
	"fmt"
	"github.com/rs/zerolog"
	"github.com/tillkuhn/angkor/tools/imagine/s3"
	"github.com/tillkuhn/angkor/tools/imagine/server"
	"github.com/tillkuhn/angkor/tools/imagine/types"
	"net/http"
	"os"
	"os/signal"
	"runtime"
	"syscall"

	"github.com/tillkuhn/angkor/tools/imagine/auth"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"
	"github.com/rs/zerolog/log"
	"github.com/tillkuhn/angkor/go/topkapi"
)

var (
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

	startMsg := fmt.Sprintf("Started service [%s] build=%s PID=%d OS=%s loglevel=%s",
		AppId, BuildTime, os.Getpid(), runtime.GOOS, zerolog.GlobalLevel().String())
	mainLogger.Info().Msg(startMsg)

	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	var config types.Config
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

	// Setup AWS and init S3 Upload Worker
	mainLogger.Info().Msgf("Establish AWS session target bucket=%s prefix=%s", config.S3Bucket, config.S3Prefix)
	awsSession, errAWS := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if errAWS != nil {
		mainLogger.Fatal().Msgf("session.NewSession (AWS) err: %v", errAWS)
	}
	s3Handler := s3.NewHandler(awsSession, kafkaClient, &config)
	// Start S3 Upload Worker Queue goroutine with buffered Queue
	mainLogger.Info().Msgf("Starting S3 Upload Worker queue with capacity=%d", config.QueueSize)
	go s3Handler.StartWorker()

	// Setup Auth and HTTP Handler
	httpHandler := server.NewHandler(s3Handler, &config)
	authContext := auth.NewHandlerContext(config.EnableAuth, config.JwksEndpoint)

	// Route for Health info
	router.HandleFunc(cp+"/health", server.Health).Methods(http.MethodGet)

	// Redirect to presigned url for a particular song (protected)
	// router.HandleFunc(cp+"/songs/{item}", authContext.AuthValidationMiddleware(GetSongPresignUrl)).Methods(http.MethodGet)
	router.HandleFunc(cp+"/songs/{folder}/{item}", authContext.AuthValidationMiddleware(httpHandler.GetSongPresignUrl)).Methods(http.MethodGet)
	router.HandleFunc(cp+"/{rootFolder}/", authContext.AuthValidationMiddleware(httpHandler.ListFolders)).Methods(http.MethodGet)

	// Get All Songs as json formatted list
	router.HandleFunc(cp+"/songs/{folder}/", authContext.AuthValidationMiddleware(httpHandler.ListSongs)).Methods(http.MethodGet)

	// Redirect to presigned url for a particular file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", httpHandler.GetObjectPresignUrl).Methods(http.MethodGet)

	// Delete file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", httpHandler.DeleteObject).Methods(http.MethodDelete)

	// Get All file objects as json formatted list
	router.HandleFunc(cp+"/{entityType}/{entityId}", httpHandler.ListObjects).Methods(http.MethodGet)

	// Upload new file multipart via POST Request
	router.HandleFunc(cp+"/{entityType}/{entityId}", authContext.AuthValidationMiddleware(httpHandler.PostObject)).Methods(http.MethodPost)

	// Upload new Song via POST Request
	router.HandleFunc(cp+"/songs", authContext.AuthValidationMiddleware(httpHandler.PostSong)).Methods(http.MethodPost)

	// Serve Static files (mainly for local dev if directory ./static is present)
	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		mainLogger.Printf("No Static dir /static, running only as API Server ")
	} else {
		mainLogger.Printf("Setting up route to local /static directory")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}
	// Show all routes so we know what's served today
	dumpRoutes(router)

	// Launch re-tagger in separate go routine
	go s3Handler.Retag()

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
