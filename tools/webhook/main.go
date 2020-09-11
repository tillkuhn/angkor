package main

import (
	"bytes"
	"fmt"
	log "github.com/sirupsen/logrus"
	"net/http"
	"os/exec"
	"strings"
)

const (
	// port sould match with infra/modules/ec2/variables.tf
	DefaultListenaddress = ":5000"
	certRoot             = "" // todo
)

func hello(w http.ResponseWriter, req *http.Request) {

	fmt.Fprintf(w, "hello\n")
}

func hook(w http.ResponseWriter, req *http.Request) {

	fmt.Fprintf(w, "hook has been called\n")
	cmd := exec.Command("docker-compose", "-v")
	cmd.Stdin = strings.NewReader("some input")
	var out bytes.Buffer
	cmd.Stdout = &out
	err := cmd.Run()
	if err != nil {
		log.Fatal(err)
	}
	// fmt.Printf("in all caps: %q\n", out.String())
	log.Infof("hook is called agent %v compose version %v", req.UserAgent(), out.String())
	fmt.Fprintf(w, "%v\n", out.String())
}

func headers(w http.ResponseWriter, req *http.Request) {

	for name, headers := range req.Header {
		for _, h := range headers {
			fmt.Fprintf(w, "%v: %v\n", name, h)
		}
	}
}

func main() {

	http.HandleFunc("/hello", hello)
	http.HandleFunc("/hook", hook)
	log.Infof("Serving on %v", DefaultListenaddress)
	// http.ListenAndServe(DefaultListenaddress, nil)
	// https://gist.github.com/denji/12b3a568f092ab951456n
	// Display cert  curl -vvI https://localhost:8443/hello
	err := http.ListenAndServeTLS(DefaultListenaddress, certRoot+"/fullchain.pem", certRoot+"/privkey.pem", nil)
	if err != nil {
		log.Fatal(err)
	}
}
