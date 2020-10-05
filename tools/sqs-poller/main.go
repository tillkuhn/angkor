package main

import (
	"context"
	"fmt"
	"os"
	"strconv"

	"github.com/tillkuhn/angkor/tools/sqs-poller/worker"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
)

func main() {
	awsConfig := &aws.Config{
		// Credentials: credentials.NewStaticCredentials(os.Getenv("AWS_ACCESS_KEY"), os.Getenv("AWS_SECRET_KEY"), ""),
		Region: aws.String(getenv("AWS_REGION", "eu-central-1")),
	}
	sqsClient := worker.CreateSqsClient(awsConfig)
	waitTime, _ := strconv.ParseInt(getenv("SQS_POLLER_WAIT_SECONDS", "20"), 10, 64)
	workerConfig := &worker.Config{
		QueueName: getenv("SQS_POLLER_QUEUE_NAME", "angkor-events"),
		//QueueURL: "https://sqs.eu-central-1.amazonaws.com/account/xz",
		MaxNumberOfMessage: 10, // max 10
		WaitTimeSecond:     waitTime,
		SleepTimeSecond: 40,
	}
	eventWorker := worker.New(sqsClient, workerConfig)
	ctx := context.Background()

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		fmt.Println(aws.StringValue(msg.Body))
		return nil
	}))
}

func getenv(key, fallback string) string {
	value := os.Getenv(key)
	if len(value) == 0 {
		return fallback
	}
	return value
}
