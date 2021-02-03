package worker

import (
	"fmt"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/aws/aws-sdk-go/service/sqs/sqsiface"
)

// CreateSqsClient creates a client for SQS API
func CreateSqsClient(awsConfigs ...*aws.Config) sqsiface.SQSAPI {
	awsSession := session.Must(session.NewSession())

	return sqs.New(awsSession, awsConfigs...)
}

func (config *Config) populateDefaultValues() {
	if config.MaxMessages == 0 {
		config.MaxMessages = 10
	}

	if config.WaitSeconds== 0 {
		config.WaitSeconds = 20
	}
}

func getQueueURL(client sqsiface.SQSAPI, queueName string) (queueURL string) {
	params := &sqs.GetQueueUrlInput{
		QueueName: aws.String(queueName), // Required
	}
	response, err := client.GetQueueUrl(params)
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	queueURL = aws.StringValue(response.QueueUrl)

	return
}
