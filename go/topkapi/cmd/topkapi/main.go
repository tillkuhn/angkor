package main

import (
	"flag"
	"fmt"
	"github.com/Shopify/sarama"
	cloudevents "github.com/cloudevents/sdk-go/v2"
	"github.com/cloudevents/sdk-go/v2/event"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"io"
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
	AppId       = "topkapicli" // path.Base(os.Args[0])

	// CLI params Parsed from flags ...
	topic          string
	message        string
	action         string
	source         string
	help           bool
	verbose        bool
	format    	   string
	consumerTimout string // must be compatible with time.ParseDuration
	mainLogger   zerolog.Logger
)

func main() {
	zerolog.SetGlobalLevel(zerolog.DebugLevel)
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr, TimeFormat: "Jan-02 15:04:05"}).With().Str("app", AppId).Logger()
	mainLogger = log.Logger.With().Str("cmp","main").Logger()

	mainLogger.Printf("%s CLI build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	flag.StringVar(&topic, "topic", "default", "The topic to publish to")
	flag.StringVar(&action, "action", "", "The event's action")
	flag.StringVar(&message, "message", "", "The event message")
	flag.StringVar(&source, "source", AppId, "Identifier for the event source (and clientId)")
	flag.StringVar(&consumerTimout, "timeout", "0s", "Timeout Duration for consumer (e.g. 11s), leave empty to block forever")
	flag.BoolVar(&help, "h", false, "Display help and exit")
	flag.BoolVar(&verbose, "v", false, "Turn on verbose logging for sarama client")
	flag.StringVar(&format, "format", "classic", "event format (classic or cloudevents")
	consumeMode := flag.Bool("consume", false, "If true, consumes messages (default false)")
	flag.Parse()

	client := topkapi.NewClientWithId(source)
	defer client.Close()
	client.Verbose(verbose)
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
		mainLogger.Print("Data is being piped to stdin")
		byteMessage, _ := io.ReadAll(io.Reader(os.Stdin))
		message = string(byteMessage)
	} else {
		// Stdin is from a terminal
		if message == "" {
			printUsageErrorAndExit("Message flag is required unless data is piped to stdin")
		}
	}
	if format == "cloudevents" {
		cloudEvent := newCloudEvent(action,message)
		json,_ := cloudEvent.MarshalJSON()
		_, _, err := client.PublishMessage(json, topic)
		if err != nil {
			mainLogger.Fatal().Msgf("Error publishing to %s: %v", topic, err)
		}
	} else {
		_, _, err := client.PublishEvent(client.NewEvent(action, message), topic)
		if err != nil {
			mainLogger.Fatal().Msgf("Error publishing to %s: %v", topic, err)
		}
	}
}

func consume(client *topkapi.Client) {
	client.Config.OffsetMode = "oldest" // default is 'newest'
	client.Config.ConsumerTimeout, _ = time.ParseDuration(consumerTimout)
	topicsSlice := strings.Split(topic, ",")
	var messageHandler topkapi.MessageHandler = func(message *sarama.ConsumerMessage) {
		log.Printf("Consumed Message: value=%s, timestamp=%v, topic=%s headers=%v",
			string(message.Value), message.Timestamp, message.Topic, len(message.Headers))
	}
	if err := client.Consume(messageHandler, topicsSlice...); err != nil {
		mainLogger.Fatal().Msgf("Error Consuming from %s: %v", topic, err)
	}
}

func newCloudEvent(action string, message string) *event.Event {
	// Create an Event.
	cEvent := cloudevents.NewEvent()
	cEvent.SetSource("example/uri")
	cEvent.SetType("example.type")
	err := cEvent.SetData(cloudevents.ApplicationJSON, map[string]string{
		"message":message,
		"action":action,
	})
	if err != nil {
		return nil
	}
	return &cEvent
}


func printUsageErrorAndExit(message string) {
	if _, err := fmt.Fprintln(os.Stderr, "ERROR:", message, "\n", "Available command line options:"); err != nil {
		mainLogger.Printf("Cannot write to stderr: %s", err.Error())
	}
	flag.PrintDefaults()
	os.Exit(64)
}
