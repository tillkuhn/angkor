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

type Config struct {
	AwsRegion string `default:"eu-central-1" split_words:"true" required:"true" desc:"AWS Region"`
	QueueName string `default:"angkor-events" split_words:"true" required:"true" desc:"Name of the SQS Queue"`
	WaitSeconds int64 `default:"20" split_words:"true" required:"true" desc:"Seconds to wait for messages"`
	SleepSeconds int64 `default:"40" split_words:"true" required:"true" desc:"Seconds to sleep between runs"`
	MaxMessages int64 `default:"10" split_words:"true" required:"true" desc:"Max number of messages to fetch"`
}

func main() {
	log.Printf("Starting service [%s] build %s with PID %d", path.Base(os.Args[0]), BuildTime, os.Getpid())
	// if called with -h, dump config help exit
	var help = flag.Bool("h", false, "display help message")
	var config Config
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

	awsConfig := &aws.Config{
		// Credentials: credentials.NewStaticCredentials(os.Getenv("AWS_ACCESS_KEY"), os.Getenv("AWS_SECRET_KEY"), ""),
		Region: aws.String(config.AwsRegion),
	}
	sqsClient := worker.CreateSqsClient(awsConfig)
	workerConfig := &worker.Config{
		QueueName: config.QueueName,  //QueueURL: "https://sqs.eu-central-1.amazonaws.com/account/xz",
		MaxNumberOfMessage: config.MaxMessages,
		WaitTimeSecond:     config.WaitSeconds,
		SleepTimeSecond:    config.SleepSeconds,
	}
	eventWorker := worker.New(sqsClient, workerConfig)
	ctx := context.Background()

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		fmt.Println(aws.StringValue(msg.Body))
		return nil
	}))
}

