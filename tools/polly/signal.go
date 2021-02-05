package main

import (
	"context"
	"log"
	"os"
	"syscall"
)

func signalHandler(signalChan chan os.Signal,  cancel context.CancelFunc) {
	for {
		s := <-signalChan
		switch s {
		case syscall.SIGHUP:
			log.Printf("Received SIGHUP signal (%v), probably from restart action. Cancel Worker", s)
			// https://www.sohamkamani.com/golang/2018-06-17-golang-using-context-cancellation/#emitting-a-cancellation-event
			cancel()
		case syscall.SIGTERM:
			log.Printf("Received SIGTERM (%v), probably from systemd. Cancel Worker", s)
			// time.Sleep(2 * time.Second)
			cancel()
		default:
			log.Printf("Unexpected signal %d", s)
		}
	}
}
