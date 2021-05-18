package main

import (
	"encoding/json"
	"log"
	"os"
	"path"
	"runtime"

	"github.com/tillkuhn/angkor/tools/topkapi/pkg"
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
	em := &EventMessage{
		Event:  "Create",
		Entity: "IceCream"}
	em2B, _ := json.Marshal(em)
	pkg.Publish(em2B)
}
