package main

import (
	"context"
	"flag"
	"fmt"
	"os"
	"os/signal"
	"runtime"
	"syscall"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"

	"github.com/tillkuhn/angkor/go/topkapi"

	"github.com/tillkuhn/angkor/tools/sqs-poller/worker"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/kelseyhightower/envconfig"
)

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime = "latest"
	AppId     = "polly"
	// mLogger    = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.L std Flags)
)

func main() {
	log.Logger = log.With().Str("app", AppId).Logger().Output(zerolog.ConsoleWriter{Out: os.Stderr})
	mLogger := log.With().Str("logger", "main").Logger()

	startMsg := fmt.Sprintf("Starting service [%s] build=%s PID=%d OS=%s", AppId, BuildTime, os.Getpid(), runtime.GOOS)
	mLogger.Println(startMsg)

	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	var workerConfig worker.Config
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		_ = envconfig.Usage(AppId, &workerConfig)
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(AppId, &workerConfig)
	if err != nil {
		mLogger.Fatal().Msg(err.Error())
	}

	// Kafka event support
	client := topkapi.NewClientWithId(AppId)
	defer client.Close()
	client.Enable(workerConfig.KafkaSupport)

	if _, _, err := client.PublishEvent(client.NewEvent("startup:"+AppId, startMsg), "system"); err != nil {
		mLogger.Printf("Error publish event to %s: %v", "system", err)
	}
	// AWS Configuration
	awsConfig := &aws.Config{
		// Credentials: credentials.NewStaticCredentials(os.Getenv("AWS_ACCESS_KEY"), os.Getenv("AWS_SECRET_KEY"), ""),
		Region: aws.String(workerConfig.AwsRegion),
	}
	sqsClient := worker.CreateSqsClient(awsConfig)
	//workerConfig := &worker.Config{
	//	QueueName: config.QueueName,  //QueueURL: "https://sqs.eu-central-1.amazonaws.com/account/xz",
	//	MaxNumberOfMessage: config.MaxMessages,
	//	WaitTimeSecond:     config.WaitSeconds,
	//	SleepTimeSecond:    config.SleepSeconds,
	//}
	eventWorker := worker.New(sqsClient, &workerConfig)
	// https://www.sohamkamani.com/golang/2018-06-17-golang-using-context-cancellation/#emitting-a-cancellation-event
	// Create a new context, with its cancellation function from the original context
	ctx, cancel := context.WithCancel(context.Background())
	ctx = mLogger.WithContext(ctx) // make logger accessible to other components via context

	signalChan := make(chan os.Signal, 1)                      //https://gist.github.com/reiki4040/be3705f307d3cd136e85
	signal.Notify(signalChan, syscall.SIGHUP, syscall.SIGTERM) // 15
	go signalHandler(signalChan, cancel)                       // invokes cancel function onn sighup and sigterm

	// start the worker
	handlerFunc := func(msg *sqs.Message) error {
		// fmt.Println(aws.StringValue(msg.Body)) // we already log th message in the worker
		return nil
	}
	eventWorker.Start(ctx, worker.HandlerFunc(handlerFunc))
	mLogger.Print("Exit main, goodbye")

}
