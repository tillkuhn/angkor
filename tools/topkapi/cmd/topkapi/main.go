package main

import (
	"flag"
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
	AppId = path.Base(os.Args[0])

	topic string
	message string
	action string
	source string
)

func main() {

	log.Printf("Starting service [%s] build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
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
		usage("action is required in producer mode")
	}
	kafkaConfig := topkapi.NewConfig()
	producer := topkapi.NewProducer(kafkaConfig)
	defer producer.Close()

	stat, _ := os.Stdin.Stat()
	if (stat.Mode() & os.ModeCharDevice) == 0 {
		// https://zetcode.com/golang/pipe/  Go read standard input through pipe
		log.Println("data is being piped to stdin")
		byteMessage, _ := io.ReadAll(io.Reader(os.Stdin))
		message = string(byteMessage)
	} else {
		log.Println("stdin is from a terminal, using default test message")

		if message == "" {
			usage("message is required unless data is piped to stdin")
		}
	}

	em := &topkapi.Event{
		Action:  action,
		Source:  source,
		Message: message,
		Time:    time.Now(),
	}
	if err := producer.PublishEvent(em, topic); err != nil {
		log.Fatalf("Error publishing to %s: %v", topic, err)
	}

}

func consume() {
	kafkaConfig := topkapi.NewConfig()
	consumer := topkapi.NewConsumer(kafkaConfig)
	consumer.Consume(topic)
}


func usage(msg string) {
	log.Printf("%s\nUsage:\n",msg)
	flag.PrintDefaults()
	os.Exit(1)
}
