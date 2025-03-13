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

	"github.com/aws/aws-sdk-go/service/sts"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/collectors"
	"github.com/prometheus/client_golang/prometheus/promhttp"

	"github.com/rs/zerolog"
	"github.com/tillkuhn/angkor/tools/imagine/s3"
	"github.com/tillkuhn/angkor/tools/imagine/server"
	"github.com/tillkuhn/angkor/tools/imagine/types"

	"github.com/tillkuhn/angkor/tools/imagine/auth"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"

	"github.com/gorilla/mux"
	"github.com/kelseyhightower/envconfig"
	"github.com/rs/zerolog/log"
	"github.com/tillkuhn/angkor/go/topkapi"
)

var (
	// BuildTime will be overwritten by ldflags during build, e.g. -X 'main.BuildTime=2021...'
	BuildTime = "latest"
	// AppId is used to identify app in logging, and as prefix for envconfig variables
	AppId = "imagine"
)

func main() {
	// Configure logging
	zerolog.SetGlobalLevel(zerolog.DebugLevel)
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr, TimeFormat: "Jan-02 15:04:05"}).
		With().Str("app", AppId).Logger()
	mainLogger := log.Logger.With().Str("logger", "main").Logger()

	// Catch HUP and INT signals https://gist.github.com/reiki4040/be3705f307d3cd136e85
	// to send programmatically: signalChan <- syscall.SIGHUP
	mainLogger.Info().Msgf("[INIT] Setting up signal handler for sigs %d and %d", syscall.SIGHUP, syscall.SIGINT)
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, syscall.SIGHUP, syscall.SIGINT)
	go func() {
		for {
			s := <-signalChan
			switch s {
			case syscall.SIGHUP: // kill -SIGHUP pid
				mainLogger.Info().Msgf("Received hangover signal (%v), maybe do something in future", s)
			case syscall.SIGINT: // kill -SIGINT pid
				mainLogger.Info().Msgf("Received SIGINT (%v), terminating", s)
				os.Exit(2)
			default: // Any other signal
				mainLogger.Warn().Msgf("Ignoring unexpected signal %d", s)
			}
		}
	}()

	// Time to announce that we're there
	startMsg := fmt.Sprintf("Started service [%s] build=%s PID=%d OS=%s loglevel=%s",
		AppId, BuildTime, os.Getpid(), runtime.GOOS, zerolog.GlobalLevel().String())
	mainLogger.Info().Msg(startMsg)

	// If called with -h, show config help and exit
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	var config types.Config
	if *help {
		if err := envconfig.Usage(AppId, &config); err != nil {
			mainLogger.Error().Msgf("Cannot write envconfig usage: %v", err)
		}
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(AppId, &config)
	if err != nil {
		mainLogger.Fatal().Msgf("Error during envconfig - cannot continue: %v", err)
	}

	// Kafka send start event to topic
	kafkaClient := topkapi.NewClientWithId(AppId)
	defer kafkaClient.Close()
	if !config.KafkaSupport {
		mainLogger.Warn().Msg("Kafka support is currently disabled, consider migration to confluent!")
	}
	kafkaClient.Enable(config.KafkaSupport)
	if _, _, err := kafkaClient.PublishEvent(kafkaClient.NewEvent("startup:"+AppId, startMsg), "system"); err != nil {
		mainLogger.Error().Msgf("[KAFKA] Error publish event to %s: %v", "system", err)
	}

	// Configure HTTP Router`
	cp := config.ContextPath
	router := mux.NewRouter()

	// Setup AWS and init S3 Upload Worker
	mainLogger.Info().Msgf("[AWS] Establish session target bucket=%s prefix=%s", config.S3Bucket, config.S3Prefix)
	awsSession, err := session.NewSession(&aws.Config{Region: aws.String(config.AWSRegion)})
	if err != nil {
		mainLogger.Fatal().Msgf("[AWS] session.NewSession  err: %v", err)
	}
	// Get Account from callerIdentity, useful for JWT to match ARNs from token info (e.g. roles)
	// https://docs.aws.amazon.com/sdk-for-go/api/service/sts/#example_STS_GetCallerIdentity_shared00
	awsIdentity, err := sts.New(awsSession).GetCallerIdentity(&sts.GetCallerIdentityInput{})
	if err != nil {
		mainLogger.Fatal().Msgf("[AWS] sts.GetCallerIdentity err: %v", err)
	}
	mainLogger.Debug().Msgf("[AWS] sts.GetCallerIdentity account=%s", *awsIdentity.Account)

	s3Handler := s3.NewHandler(awsSession, kafkaClient, &config)
	// Start S3 Upload Worker Queue goroutine with buffered Queue
	mainLogger.Info().Msgf("[AWS] Starting S3 Upload Worker queue with capacity=%d", config.QueueSize)
	go s3Handler.StartWorker()

	// Setup Auth and HTTP Handler
	httpHandler := server.NewHandler(s3Handler, &config)
	defer httpHandler.Close()
	authHandler := auth.New(config.EnableAuth, config.JwksEndpoint, *awsIdentity.Account)

	// Route for Health info
	router.HandleFunc(cp+"/health", server.Health).Methods(http.MethodGet)

	// Setup Prometheus Client
	// # TYPE promhttp_metric_handler_requests_total counter
	// promhttp_metric_handler_requests_total{code="200"} 3
	// Prometheus Preparation
	// reduce noise (default init https://github.com/prometheus/client_golang/blob/main/prometheus/registry.go#L60)
	// Default client_golang Collectors: https://povilasv.me/prometheus-go-metrics/
	// Go Collector collects information about Go’s runtime like details about GC, # of go´routines + OS threads
	prometheus.Unregister(collectors.NewGoCollector())
	// ProcessCollector collects basic Linux process information like CPU, memory, file descriptor usage and start time
	prometheus.Unregister(collectors.NewProcessCollector(collectors.ProcessCollectorOpts{}))
	pStart := prometheus.NewGauge(prometheus.GaugeOpts{
		Namespace: "angkor",
		Subsystem: AppId,
		Name:      "process_start_time_seconds",
		Help:      "Start time of the process since unix epoch",
	})
	prometheus.MustRegister(pStart)
	pStart.Add(float64(time.Now().Unix()))

	ph := http.HandlerFunc(promhttp.Handler().ServeHTTP) // need to wrap from http.Handler to HandlerFunc
	// ph := http.HandlerFunc(promhttp.HandlerFor(reg, promhttp.HandlerOpts{Registry: reg}).ServeHTTP) // need to wrap from http.Handler to HandlerFunc

	metricsToken := os.Getenv("IMAGINE_API_TOKEN_METRICS")
	if metricsToken != "" {
		router.Handle(cp+"/metrics", authHandler.CheckTokenMiddleware(metricsToken, ph)).Methods(http.MethodGet)
	} else {
		log.Warn().Msgf("IMAGINE_API_TOKEN_METRICS not set, cannot expose prometheus metrics handler")
	}
	// Redirect to presigned url for a particular song (protected)
	// router.HandleFunc(cp+"/songs/{item}", authHandler.ValidationMiddleware(GetSongPresignUrl)).Methods(http.MethodGet)
	router.HandleFunc(cp+"/songs/{folder}/{item}", authHandler.ValidationMiddleware(httpHandler.GetSongPresignUrl)).Methods(http.MethodGet)
	router.HandleFunc(cp+"/{rootFolder}/", authHandler.ValidationMiddleware(httpHandler.ListFolders)).Methods(http.MethodGet)

	// Get All Songs as json formatted list
	router.HandleFunc(cp+"/songs/{folder}/", authHandler.ValidationMiddleware(httpHandler.ListSongs)).Methods(http.MethodGet)

	// Redirect to presigned url for a particular file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", httpHandler.GetObjectPresignUrl).Methods(http.MethodGet)

	// Delete file
	router.HandleFunc(cp+"/{entityType}/{entityId}/{item}", httpHandler.DeleteObject).Methods(http.MethodDelete)

	// Get All file objects as json formatted list
	router.HandleFunc(cp+"/{entityType}/{entityId}", httpHandler.ListObjects).Methods(http.MethodGet)

	// Upload new file multipart via POST Request
	router.HandleFunc(cp+"/{entityType}/{entityId}", authHandler.ValidationMiddleware(httpHandler.PostObject)).Methods(http.MethodPost)

	// Upload new Song via POST Request
	router.HandleFunc(cp+"/songs", authHandler.ValidationMiddleware(httpHandler.PostSong)).Methods(http.MethodPost)

	// Serve Static files (mainly for local dev if directory ./static is present)
	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		mainLogger.Printf("No Static dir /static in local fs, running only as API Server ")
	} else {
		mainLogger.Debug().Msgf("[HTTP] Setting up route to local /static directory")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}
	// Show all HTTP routes, so we know what's served today
	dumpRoutes(router)

	// Launch re-tagger in separate go routine
	if config.MP3Retag {
		go s3Handler.Retag()
	} else {
		log.Warn().Msg("[RETAG] MP3 Retag goroutine is disabled by config")
	}

	// Launch the HTTP Server and block
	mainLogger.Info().Msgf("[HTTP] Start HTTPServer http://localhost:%d%s", config.Port, config.ContextPath)
	srv := &http.Server{
		Handler:      router,
		Addr:         fmt.Sprintf(":%d", config.Port),
		WriteTimeout: config.Timeout,
		ReadTimeout:  config.Timeout,
	}
	mainLogger.Fatal().Err(srv.ListenAndServe())
}

// dumpRoutes Helper function to show each route + method, https://github.com/gorilla/mux/issues/186
func dumpRoutes(r *mux.Router) {
	if err := r.Walk(func(route *mux.Route, router *mux.Router, ancestors []*mux.Route) error {
		t, _ := route.GetPathTemplate()
		m, _ := route.GetMethods()
		log.Debug().Msgf("[HTTP] Registered route: %v %s", m, t)
		return nil
	}); err != nil {
		log.Printf(err.Error())
	}
}
