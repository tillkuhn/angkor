package main

import (
	"context"
	"fmt"
	"os"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/h2ik/go-sqs-poller/v3/worker"
)

func main() {
	awsConfig := &aws.Config{
		// Credentials: credentials.NewStaticCredentials(os.Getenv("AWS_ACCESS_KEY"), os.Getenv("AWS_SECRET_KEY"), ""),
		Region:      aws.String(os.Getenv("AWS_REGION")),
	}
	sqsClient := worker.CreateSqsClient(awsConfig)
	workerConfig := &worker.Config{
		QueueName:          "angkor-deploy", // todo make variable
		//QueueURL: "https://sqs.eu-central-1.amazonaws.com/account/xz",
		MaxNumberOfMessage: 10, // max 10
		WaitTimeSecond:     5,
	}
	eventWorker := worker.New(sqsClient, workerConfig)
	ctx := context.Background()

	// start the worker
	eventWorker.Start(ctx, worker.HandlerFunc(func(msg *sqs.Message) error {
		fmt.Println(aws.StringValue(msg.Body))
		return nil
	}))
}
