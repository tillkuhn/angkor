package main

import (
	"encoding/json"
	"github.com/tillkuhn/angkor/tools/topkapi/pkg"
	"io"
	"log"
	"os"
	"path"
	"runtime"
)

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime = "now"
	// AppVersion should follow semver
	AppVersion = "latest"
	// ReleaseName can be anything nice
	ReleaseName = "pura-vida"
)

type EventMessage struct {
	Event  string `json:"event"`
	Entity string `json:"entity"`
}

func main() {
	log.Printf("Starting service [%s] build=%s Version=%s Rel=%s PID=%d OS=%s", path.Base(os.Args[0]), AppVersion, ReleaseName, BuildTime, os.Getpid(), runtime.GOOS)
	config := pkg.NewConfig()
	var byteMessage []byte
	stat, _ := os.Stdin.Stat()
	if (stat.Mode() & os.ModeCharDevice) == 0 {
		// https://zetcode.com/golang/pipe/  Go read standard input through pipe
		log.Println("data is being piped to stdin")
		byteMessage, _ = io.ReadAll(io.Reader(os.Stdin))
	} else {
		log.Println("stdin is from a terminal, using default test message")
		em := &EventMessage{
			Event:  "Create",
			Entity: "IceCream"}
		byteMessage, _ = json.Marshal(em)
	}
	topic := config.SaslUsername + "-system"
	pkg.Publish(byteMessage, topic, config)
	// confluent.Publish(byteMessage)
}

