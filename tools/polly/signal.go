package main

import (
	"context"
	"log"
	"os"
	"syscall"
	"time"
)

func signalHandler(signalChan chan os.Signal, context context.Context) {
	for {
		s := <-signalChan
		switch s {
		// kill -SIGHUP XXXX
		case syscall.SIGHUP:
			log.Printf("Received hangover signal (%v), let's do something", s)
		// kill -SIGTERM XXXX
		case syscall.SIGTERM:
			log.Printf("Received SIGTERM (%v), terminating", s)
			context.Done()
			time.Sleep(2 * time.Second)
			os.Exit(0)
		default:
			log.Printf("Unexpected signal %d", s)
		}
	}
}
