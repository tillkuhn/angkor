package main

import (
	"context"
	"flag"
	"log"
	"os"
	"os/signal"
	"path"
	"syscall"

	"github.com/tillkuhn/angkor/tools/sqs-poller/worker"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "polly"

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime string = "latest"
)

func main() {
	log.Printf("Starting service [%s] build %s with PID %d", path.Base(os.Args[0]), BuildTime, os.Getpid())
	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	var workerConfig worker.Config
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		envconfig.Usage(appPrefix, &workerConfig)
		os.Exit(0)
	}

	// Parse config based on Environment Variables
	err := envconfig.Process(appPrefix, &workerConfig)
	if err != nil {
		log.Fatal(err.Error())
	}

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

	signalChan := make(chan os.Signal, 1) //https://gist.github.com/reiki4040/be3705f307d3cd136e85
	signal.Notify(signalChan, syscall.SIGHUP, syscall.SIGTERM) // 15
	go signalHandler(signalChan,cancel)

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		// fmt.Println(aws.StringValue(msg.Body)) // we already log th message in the worker
		return nil
	}))
}
