package worker

import (
	// "bytes"
	"context"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"sync"
	"time"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/aws/aws-sdk-go/service/sqs/sqsiface"
)

// HandlerFunc is used to define the Handler that is run on for each message
type HandlerFunc func(msg *sqs.Message) error

// HandleMessage wraps a function for handling sqs messages
//  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${appid}-api
func (f HandlerFunc) HandleMessage(msg *sqs.Message) error {
	// Todo ann docker-compose pull first to ensure we get latest image
	// See dicussion here https://github.com/docker/compose/issues/3574
	// fmt.Printf("in all caps: %q\n", out.String())
	pullout, puller := localExec("docker-compose", "pull", "--quiet") // out is byte[]
	if puller != nil {
		log.Printf("ERROR during pull %v", puller)
	}
	log.Printf("SQS triggered docker-compose compose pull output %v\n", string(pullout))
	upout, uperr := localExec("docker-compose", "up", "--detach", "--quiet-pull") // out is byte[]
	if uperr != nil {
		log.Printf("ERROR during pull %v", uperr)
	}
	log.Printf("SQS triggered docker-compose compose up output %v\n", string(upout))

	return f(msg)
}

func localExec(name string, arg ...string) ([]byte, error) {
	currentDir, errDir := os.Getwd()
	if errDir != nil {
		log.Fatal(errDir)
	}
	// fmt.Printf("Handling message in currentDir %s",currentDir)
	cmd := exec.Command(name, arg...)
	cmd.Env = os.Environ()
	cmd.Env = append(cmd.Env, "WORKDIR="+currentDir)
	cmd.Stdin = strings.NewReader("some input")
	// var out bytes.Buffer
	// cmd.Stdout = &out
	// err := cmd.Run()
	return cmd.CombinedOutput() // out is byte[]
}

// Handler interface
type Handler interface {
	HandleMessage(msg *sqs.Message) error
}

// InvalidEventError struct
type InvalidEventError struct {
	event string
	msg   string
}

func (e InvalidEventError) Error() string {
	return fmt.Sprintf("[Invalid Event: %s] %s", e.event, e.msg)
}

// NewInvalidEventError creates InvalidEventError struct
func NewInvalidEventError(event, msg string) InvalidEventError {
	return InvalidEventError{event: event, msg: msg}
}

// Worker struct
type Worker struct {
	Config    *Config
	Log       LoggerIFace
	SqsClient sqsiface.SQSAPI
}

// Config struct
type Config struct {
	MaxNumberOfMessage int64
	QueueName          string
	QueueURL           string
	WaitTimeSecond     int64
	SleepTimeSecond    int64
}

// New sets up a new Worker
func New(client sqsiface.SQSAPI, config *Config) *Worker {
	config.populateDefaultValues()
	config.QueueURL = getQueueURL(client, config.QueueName)

	return &Worker{
		Config:    config,
		Log:       &logger{},
		SqsClient: client,
	}
}

// Start starts the polling and will continue polling till the application is forcibly stopped
func (worker *Worker) Start(ctx context.Context, h Handler) {
	for {
		select {
		case <-ctx.Done():
			worker.Log.Info("worker: Stopping polling because a context kill signal was sent")
			return
		default:
			worker.Log.Debug(fmt.Sprintf("polly: start polling queue %s interval %ds, sleep %ds",
				worker.Config.QueueName, worker.Config.WaitTimeSecond, worker.Config.SleepTimeSecond))

			params := &sqs.ReceiveMessageInput{
				QueueUrl:            aws.String(worker.Config.QueueURL), // Required
				MaxNumberOfMessages: aws.Int64(worker.Config.MaxNumberOfMessage),
				AttributeNames: []*string{
					aws.String("All"), // Required
				},
				WaitTimeSeconds: aws.Int64(worker.Config.WaitTimeSecond),
			}

			resp, err := worker.SqsClient.ReceiveMessage(params)
			if err != nil {
				worker.Log.Error(fmt.Sprintf("Error during receive: %v",err))
				if strings.Contains(err.Error(),"InvalidAddress") {
					worker.Log.Error("Cannot recover from InvalidAddress, exit")
					return
				}
				continue
			}
			if len(resp.Messages) > 0 {
				worker.run(h, resp.Messages)
			}
		}
		time.Sleep(time.Duration(worker.Config.SleepTimeSecond) * time.Second)
	}
}

// poll launches goroutine per received message and wait for all message to be processed
func (worker *Worker) run(h Handler, messages []*sqs.Message) {
	numMessages := len(messages)
	worker.Log.Info(fmt.Sprintf("worker: Received %d messages", numMessages))

	var wg sync.WaitGroup
	wg.Add(numMessages)
	for i := range messages {
		go func(m *sqs.Message) {
			// launch goroutine
			defer wg.Done()
			if err := worker.handleMessage(m, h); err != nil {
				worker.Log.Error(err.Error())
			}
		}(messages[i])
	}

	wg.Wait()
}

func (worker *Worker) handleMessage(m *sqs.Message, h Handler) error {
	var err error
	err = h.HandleMessage(m)
	if _, ok := err.(InvalidEventError); ok {
		worker.Log.Error(err.Error())
	} else if err != nil {
		return err
	}

	params := &sqs.DeleteMessageInput{
		QueueUrl:      aws.String(worker.Config.QueueURL), // Required
		ReceiptHandle: m.ReceiptHandle,                    // Required
	}
	_, err = worker.SqsClient.DeleteMessage(params)
	if err != nil {
		return err
	}
	worker.Log.Debug(fmt.Sprintf("worker: Removed message from queue: %s", aws.StringValue(m.ReceiptHandle)))

	return nil
}
