package main

import (
	"flag"
	"fmt"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"io"
	"log"
	"os"
	"path"
	"runtime"
	"time"
)

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime = "now"
	// AppVersion should follow semver
	AppVersion = "latest"
	// ReleaseName can be anything nice
	ReleaseName = "pura-vida"
	AppId       = path.Base(os.Args[0])
	logger      = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)

	topic   string
	message string
	action  string
	source  string
)

func main() {

	logger.Printf("Starting service [%s] build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	flag.StringVar(&topic, "topic", "default", "The topic to publish to")
	flag.StringVar(&action, "action", "", "The event's action")
	flag.StringVar(&message, "message", "", "The event message")
	flag.StringVar(&source, "source", AppId, "Identifier for the event source")
	consumeMode := flag.Bool("consume", false, "If true, consumes messages (default false)")
	flag.Parse()

	if *consumeMode {
		consume()
	} else {
		produce()
	}

}

func produce() {
	if action == "" {
		usage("Action flag is required in producer mode")
	}
	client := topkapi.NewClient()
	// client.Disable()
	defer client.Close()

	stat, _ := os.Stdin.Stat()
	if (stat.Mode() & os.ModeCharDevice) == 0 {
		// https://zetcode.com/golang/pipe/  Go read standard input through pipe
		logger.Println("Data is being piped to stdin")
		byteMessage, _ := io.ReadAll(io.Reader(os.Stdin))
		message = string(byteMessage)
	} else {
		// Stdin is from a terminal

		if message == "" {
			usage("Message flag is required unless data is piped to stdin")
		}
	}

	em := &topkapi.Event{
		Action:  action,
		Source:  source,
		Message: message,
		Time:    time.Now(),
	}
	_, _, err := client.PublishEvent(em, topic)
	if err != nil {
		logger.Fatalf("Error publishing to %s: %v", topic, err)
	}

}

func consume() {
	kafkaConfig := topkapi.NewConfig()
	client := topkapi.NewClientFromConfig(kafkaConfig)
	defer client.Close()
	client.Consume(topic)
}

func usage(msg string) {
	logger.Printf("%s\nUsage:\n", msg)
	flag.PrintDefaults()
	os.Exit(1)
}
