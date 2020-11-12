package main

import (
	"encoding/json"
	"fmt"
	"github.com/kelseyhightower/envconfig"
	"log"
	"net/http"
	"sync"
	"time"
)

const appPrefix  = "healthbells"

type urlStatus struct {
	url    string
	status bool
}

// see https://github.com/kelseyhightower/envconfig
type Config struct {
	Debug    bool          // e.g. HEALTHBELLS_DEBUG=true
	Port     int           `default:"8091"`
	Interval time.Duration `default:"-1ms"` // e.g. HEALTHBELLS_INTERVAL=5s
	Urls     []string      `default:"https://www.timafe.net/,https://timafe.wordpress.com/"`
	//ColorCodes  map[string]int // e.g. ="red:1,green:2,blue:3"
}

type CheckResult struct {
	healthy bool
	responseTime time.Duration
	checkTime time.Time
}

// https://stackoverflow.com/questions/17890830/golang-shared-communication-in-async-http-server/17930344
type HealthStatus struct {
	*sync.Mutex                    // inherits locking methods
	Results map[string]CheckResult // map ids to values
}

var (
	healthStatus = &HealthStatus{&sync.Mutex{}, map[string]CheckResult{}}
	quitChanel   = make(chan struct{})
)

func main() {
	var config Config
	err := envconfig.Process(appPrefix, &config)
	// todo explain envconfig.Usage()
	if err != nil {
		log.Fatal(err.Error())
	}
	log.Printf("Debug %v Port %d Interval %d",config.Debug,config.Port,config.Interval)

	if config.Interval >= 0 {
		log.Printf("Setup timer for interval %v",config.Interval)
		ticker := time.NewTicker(config.Interval)
		go func() {
			for {
				select {
				case <- ticker.C:
					checkAllUrls(config.Urls)
				case <- quitChanel:
					ticker.Stop()
					log.Printf("Check Loop stopped")
					return
				}
			}
		}()
		log.Printf("Running HTTP Server Listen on :%d",config.Port)
		http.HandleFunc("/", status)
		http.HandleFunc("/suspend", suspend)
		http.HandleFunc("/health", health)
		srv := &http.Server{
			Addr:         fmt.Sprintf(":%d",config.Port),
			WriteTimeout: 15 * time.Second,
			ReadTimeout:  15 * time.Second,
		}
		log.Fatal(srv.ListenAndServe())
	} else {
		checkAllUrls(config.Urls) // check onlny once
	}
}

func checkAllUrls(urls []string) {
	urlChannel := make(chan urlStatus)
	for _, url := range urls {
		go checkUrl(url, urlChannel)
	}
	result := make([]urlStatus, len(urls))
	for i, _ := range result {
		result[i] = <-urlChannel
		if result[i].status {
			log.Printf("💖 %s %s", result[i].url, "is up.")
		} else {
			log.Printf("⚡ %s %s", result[i].url, "is down !!")
		}
	}
}

//checks and prints a message if a website is up or down
func checkUrl(url string, c chan urlStatus) {
	start := time.Now()
	_, err := http.Get(url)
	elapsed := time.Since(start)
	var checkResult= new(CheckResult)
	checkResult.responseTime = elapsed
	checkResult.checkTime=time.Now()
	if err != nil {
		// The website is down
		c <- urlStatus{url, false}
		checkResult.healthy = false
	} else {
		// The website is up
		c <- urlStatus{url, true}
		checkResult.healthy = true
	}
	healthStatus.Lock()
	defer healthStatus.Unlock()
	healthStatus.Results[url]=*checkResult
}

// Standard http functions

func suspend(w http.ResponseWriter, req *http.Request) {
	log.Printf("Suspending checkloop")
	close(quitChanel)
}


func status(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	// https://purecss.io/start/
	fmt.Fprintf(w,`<html>
<head>
<link rel='stylesheet' href='https://unpkg.com/purecss@2.0.3/build/pure-min.css' crossorigin='anonymous'></link>
</head>
<body>
<table class='pure-table pure-table-horizontal'>
<thead><tr><th>Target</th><th>Checktime</th><th>Healthy</th><th>time2repond</th></tr></thead>
`)
	for key, element := range healthStatus.Results {
		fmt.Fprintf(w, "<tr><td>%s</td><td>%s</td><td>%v</td><td>%v</td></tr>",
			key,
			element.checkTime.Format(time.RFC3339),
			element.healthy,
			element.responseTime)
	}
	fmt.Fprintf(w,`</table></body></html>`)
}

func health(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	status,err := json.Marshal(map[string]interface{}{
		"status": "up",
		"info": fmt.Sprintf("%s is healthy",appPrefix),
		"time": time.Now().Format(time.RFC3339),
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Write(status)
}
