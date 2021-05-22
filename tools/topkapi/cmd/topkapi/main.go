package main

import (
	"flag"
	"fmt"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"io"
	"log"
	"os"
	"runtime"
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

	topic   string
	message string
	action  string
	source  string
)

func main() {

	logger.Printf("%s CLI build=%s Version=%s Rel=%s PID=%d OS=%s", AppId, AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	flag.StringVar(&topic, "topic", "default", "The topic to publish to")
	flag.StringVar(&action, "action", "", "The event's action")
	flag.StringVar(&message, "message", "", "The event message")
	flag.StringVar(&source, "source", AppId, "Identifier for the event source")
	consumeMode := flag.Bool("consume", false, "If true, consumes messages (default false)")
	flag.Parse()

	client := topkapi.NewClient()
	client.Verbose(false)
	client.DefaultSource(AppId)
	defer client.Close()

	if *consumeMode {
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
	client.Config.DefaultOffset = "oldest" // default is newesz
	client.Consume(topic)
}

func printUsageErrorAndExit(message string) {
	fmt.Fprintln(os.Stderr, "ERROR:", message,"\n","Available command line options:")
	flag.PrintDefaults()
	os.Exit(64)
}
