package worker

import (
	// "bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/aws/aws-sdk-go/service/sqs/sqsiface"
)

// PollyEvent is a typcial jcon message
type PollyEvent struct {
	Action   string `json:"action"`
	Workflow string `json:"workflow"`
}

// HandlerFunc is used to define the Handler that is run on for each message
type HandlerFunc func(msg *sqs.Message) error

// HandleMessage wraps a function for handling sqs messages
//  docker-compose --file ${WORKDIR}/docker-compose.yml up --detach ${appid}-api
func (f HandlerFunc) HandleMessage(msg *sqs.Message, config Config) error {

	log.Printf("SQS Message Body: %v", *msg.Body)
	request := PollyEvent{}
	json.Unmarshal([]byte(*msg.Body), &request)
	upout, uperr := localExec(config.Delegate, request.Action) // out is byte[]
	if uperr != nil {
		log.Printf("ERROR during %s %s: %v", config.Delegate, request.Action, uperr)
	}
	log.Printf("SQS triggered %s %s workflow=%s Output:", config.Delegate, request.Action, request.Workflow)
	log.Printf("%s", string(upout))

	if (len(request.Action) > 0) && (request.Action == config.RestartAction) {
		// https://github.com/golang/go/issues/19326
		p, err := os.FindProcess(os.Getpid())
		if err != nil {
			return err
		}
		log.Printf("Receiving restart acion %s, sending SIGHUP signal to myself %v", config.RestartAction, p.Pid)
		p.Signal(syscall.SIGHUP)
	}
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
	return cmd.CombinedOutput() // out is byte[]
}

// Handler interface
type Handler interface {
	HandleMessage(msg *sqs.Message, config Config) error
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
			worker.Log.Info("polly: Stopping polling because a context cancel signal was sent")
			return
		default:
			worker.Log.Debug(fmt.Sprintf("polly: start polling queue %s interval %ds, sleep %ds",
				worker.Config.QueueName, worker.Config.WaitSeconds, worker.Config.SleepSeconds))

			params := &sqs.ReceiveMessageInput{
				QueueUrl:            aws.String(worker.Config.QueueURL), // Required
				MaxNumberOfMessages: aws.Int64(worker.Config.MaxMessages),
				AttributeNames: []*string{
					aws.String("All"), // Required
				},
				WaitTimeSeconds: aws.Int64(worker.Config.WaitSeconds),
			}

			resp, err := worker.SqsClient.ReceiveMessage(params)
			if err != nil {
				worker.Log.Error(fmt.Sprintf("Error during receive: %v", err))
				if strings.Contains(err.Error(), "InvalidAddress") {
					worker.Log.Error("Cannot recover from InvalidAddress, exit")
					return
				}
				continue
			}
			if len(resp.Messages) > 0 {
				worker.run(h, resp.Messages)
			}
		}
		time.Sleep(time.Duration(worker.Config.SleepSeconds) * time.Second)
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
	err = h.HandleMessage(m, *worker.Config)
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
