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
)

func main() {

	log.Printf("Starting service [%s] build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	topic := flag.String("topic", "default", "The topic to publish to")
	action := flag.String("action", "", "The event's action")
	message := flag.String("message", "", "The event message")
	kafkaConfig := topkapi.NewConfig()
	flag.Parse()
	producer := topkapi.NewProducer(kafkaConfig)
	defer producer.Close()

	stat, _ := os.Stdin.Stat()
	if (stat.Mode() & os.ModeCharDevice) == 0 {
		// https://zetcode.com/golang/pipe/  Go read standard input through pipe
		log.Println("data is being piped to stdin")
		byteMessage, _ := io.ReadAll(io.Reader(os.Stdin))
		if err := producer.PublishMessage(byteMessage, *topic); err != nil {
			log.Fatalf("Error publishing to %s: %v",*topic,err)
		}
	} else {
		log.Println("stdin is from a terminal, using default test message")
		if *action == "" || *message == "" {
			flag.PrintDefaults()
			os.Exit(1)
		}
		em := &topkapi.Event{
			Action:  *action,
			Source: AppId,
			Message: *message,
			Time: time.Now(),
		}
		if err := producer.PublishEvent(em, *topic); err != nil {
			log.Fatalf("Error publishing to %s: %v",*topic,err)
		}
	}

}

