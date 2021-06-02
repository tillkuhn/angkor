package main

import (
	"flag"
	"fmt"
	"github.com/Shopify/sarama"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"io"
	"log"
	"os"
	"runtime"
	"strings"
	"time"
)

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime = "now"
	// AppVersion should follow semver
	AppVersion = "latest"
	// ReleaseName can be anything nice
	ReleaseName = "pura-vida"
	AppId       = "topkapi" // path.Base(os.Args[0])
	logger      = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)

	// CLI params Parsed from flags ...
	topic   string
	message string
	action  string
	source  string
	help    bool
)

func main() {

	logger.Printf("%s CLI build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	flag.StringVar(&topic, "topic", "default", "The topic to publish to")
	flag.StringVar(&action, "action", "", "The event's action")
	flag.StringVar(&message, "message", "", "The event message")
	flag.StringVar(&source, "source", AppId, "Identifier for the event source")
	flag.BoolVar(&help, "h", false, "Display help and exit")
	consumeMode := flag.Bool("consume", false, "If true, consumes messages (default false)")
	flag.Parse()

	client := topkapi.NewClientWithId(AppId)
	defer client.Close()
	if help {
		flag.Usage()
		fmt.Println("\nKafka Client Config via Environment")
		client.Usage()
	} else if *consumeMode {
		consume(client)
	} else {
		produce(client)
	}

}

func produce(client *topkapi.Client) {
	if action == "" {
		printUsageErrorAndExit("Action flag is required in producer mode")
	}

	stat, _ := os.Stdin.Stat()
	if (stat.Mode() & os.ModeCharDevice) == 0 {
		// https://zetcode.com/golang/pipe/  Go read standard input through pipe
		logger.Println("Data is being piped to stdin")
		byteMessage, _ := io.ReadAll(io.Reader(os.Stdin))
		message = string(byteMessage)
	} else {
		// Stdin is from a terminal
		if message == "" {
			printUsageErrorAndExit("Message flag is required unless data is piped to stdin")
		}
	}
	_, _, err := client.PublishEvent(client.NewEvent(action,message), topic)
	if err != nil {
		logger.Fatalf("Error publishing to %s: %v", topic, err)
	}

}

func consume(client *topkapi.Client) {
	client.Config.OffsetMode = "oldest" // default is 'newest'
	client.Config.ConsumerTimeout = 10 * time.Second
	topicsSlice := strings.Split(topic,",")
	var  messageHandler topkapi.MessageHandler = func(message *sarama.ConsumerMessage) {
		log.Printf("Consumed Message: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
	}
	if err := client.Consume(messageHandler, topicsSlice...); err != nil {
		logger.Fatalf("Error Consuming from %s: %v", topic, err)
	}
}

func printUsageErrorAndExit(message string) {
	if _, err := fmt.Fprintln(os.Stderr, "ERROR:", message,"\n","Available command line options:"); err != nil {
		logger.Printf("Cannot write to stderr: %s",err.Error())
	}
	flag.PrintDefaults()
	os.Exit(64)
}
