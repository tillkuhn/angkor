package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"os"
	"path"

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

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		fmt.Println(aws.StringValue(msg.Body))
		return nil
	}))
}
