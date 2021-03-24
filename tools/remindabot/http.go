package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"time"
)

// Fetch reminders from API
func fetchReminders(apiUrl string, apiToken string, apiTokenHeader string) (io.ReadCloser,error) {
	var myClient = &http.Client{Timeout: 10 * time.Second}
	log.Printf("Fetching notes from %s", apiUrl)
	req, _ := http.NewRequest("GET", apiUrl, nil)
	if apiToken != "" {
		req.Header.Set(apiTokenHeader, apiToken)
	}
	r, err := myClient.Do(req)
	if r == nil || r.StatusCode < 200 || r.StatusCode >= 300 {
		return nil,fmt.Errorf("http error getting %s: response=%v err=%v", apiUrl, r, err)
	}
	return r.Body,nil
}
