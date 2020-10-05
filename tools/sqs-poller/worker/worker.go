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
	currentDir, errDir := os.Getwd()
	if errDir != nil {
		log.Fatal(errDir)
	}
	// Todo ann docker-compose pull first to ensure we get latest image
	// See dicussion here https://github.com/docker/compose/issues/3574
    fmt.Printf("Handling message in currentDir %s",currentDir)
	cmd := exec.Command("docker-compose", "up","--detach","--quiet-pull")
	cmd.Env = os.Environ()
    cmd.Env = append(cmd.Env, "WORKDIR="+currentDir)
	cmd.Stdin = strings.NewReader("some input")
	// var out bytes.Buffer
	// cmd.Stdout = &out
	// err := cmd.Run()
	out, err := cmd.CombinedOutput() // out is byte[] 
	if err != nil {
		log.Printf("ERROR %v",err)
	}
	// fmt.Printf("in all caps: %q\n", out.String())
	fmt.Printf("hook is called compose output in %s: %v",currentDir, string(out))

	return f(msg)
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
			log.Println("worker: Stopping polling because a context kill signal was sent")
			return
		default:
			worker.Log.Debug(fmt.Sprintf("worker: Start Polling %s interval %ds", worker.Config.QueueName, worker.Config.WaitTimeSecond))

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
				log.Println(err)
				continue
			}
			if len(resp.Messages) > 0 {
				worker.run(h, resp.Messages)
			}
		}
		worker.Log.Debug((fmt.Sprintf("taking a %ds break before continue to poll",worker.Config.SleepTimeSecond)))
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
