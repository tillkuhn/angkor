package main

import (
	"context"
	"flag"
	"fmt"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"log"
	"os"
	"os/signal"
	"runtime"
	"syscall"

	"github.com/tillkuhn/angkor/tools/sqs-poller/worker"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/kelseyhightower/envconfig"
)


var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime   = "latest"
	AppId       = "polly"
	logger      = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)	
)

func main() {
	startMsg := fmt.Sprintf("starting service [%s] build=%s PID=%d OS=%s", AppId, BuildTime, os.Getpid(), runtime.GOOS)
	logger.Println(startMsg)

	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	var workerConfig worker.Config
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		envconfig.Usage(AppId, &workerConfig)
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(AppId, &workerConfig)
	if err != nil {
		logger.Fatal(err.Error())
	}

	// Kafka event support
	client := topkapi.NewClientWithId(AppId)
	defer client.Close()
	client.Enable(workerConfig.KafkaSupport)

	if _, _, err := client.PublishEvent(client.NewEvent("startsvc:" + AppId,startMsg),"system"); err != nil {
		logger.Printf("Error publish event to %s: %v", "system", err)
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
	ctx := context.Background()
	// https://www.sohamkamani.com/golang/2018-06-17-golang-using-context-cancellation/#emitting-a-cancellation-event
	// Create a new context, with its cancellation function from the original context
	ctx, cancel := context.WithCancel(ctx)

	signalChan := make(chan os.Signal, 1)                      //https://gist.github.com/reiki4040/be3705f307d3cd136e85
	signal.Notify(signalChan, syscall.SIGHUP, syscall.SIGTERM) // 15
	go signalHandler(signalChan, cancel)

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		// fmt.Println(aws.StringValue(msg.Body)) // we already log th message in the worker
		return nil
	}))
}
