package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"path"
	"sync"
	"time"

	"github.com/dustin/go-humanize"
	"github.com/kelseyhightower/envconfig"
)

// used as envconfig prefix and as a unique identity of this service e.g. for healthchecking
const appid = "healthbells"

// see https://github.com/kelseyhightower/envconfig
type Config struct {
	Quiet    bool          `default:"true"` // e.g. HEALTHBELLS_DEBUG=true
	Port     int           `default:"8091"`
	Interval time.Duration `default:"-1ms"` // e.g. HEALTHBELLS_INTERVAL=5s
	Timeout  time.Duration `default:"10s"`  // e.g. HEALTHBELLS_TIMEOUT=10s
	Urls     []string      `default:"https://www.timafe.net/,https://timafe.wordpress.com/"`
	Histlen  int           `default:"25"` // how many items to keep ...
}

type urlStatus struct {
	url    string
	status bool
}

type CheckResult struct {
	target       string
	healthy      bool
	responseTime time.Duration
	checkTime    time.Time
}

// https://stackoverflow.com/questions/17890830/golang-shared-communication-in-async-http-server/17930344
type HealthStatus struct {
	*sync.Mutex // inherits locking methods
	Results     []CheckResult
}

var (
	healthStatus = &HealthStatus{&sync.Mutex{}, []CheckResult{}}
	quitChanel   = make(chan struct{})
	config       Config
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime string = "latest"
)

// Let's rock ...
func main() {
	log.Printf("starting service [%s] build %s with PID %d", path.Base(os.Args[0]), BuildTime, os.Getpid())
	err := envconfig.Process(appid, &config)
	// todo explain envconfig.Usage()
	if err != nil {
		log.Fatal(err.Error())
	}
	log.Printf("Quiet %v Port %d Interval %v Timeout %v", config.Quiet, config.Port, config.Interval, config.Timeout)

	log.Printf("Initial run for %v", config.Urls)
	checkAllUrls(config.Urls) // check onlny once
	if config.Interval >= 0 {
		log.Printf("Setting up timer check interval=%v", config.Interval)
		ticker := time.NewTicker(config.Interval)
		go func() {
			for {
				select {
				case <-ticker.C:
					checkAllUrls(config.Urls)
				case <-quitChanel:
					ticker.Stop()
					log.Printf("Check Loop stopped")
					return
				}
			}
		}()
		http.HandleFunc("/", status)
		http.HandleFunc("/suspend", suspend)
		http.HandleFunc("/health", health)
		srv := &http.Server{
			Addr:         fmt.Sprintf(":%d", config.Port),
			WriteTimeout: 15 * time.Second,
			ReadTimeout:  15 * time.Second,
		}
		log.Printf("Starting HTTP Server on http://localhost:%d", config.Port)
		log.Fatal(srv.ListenAndServe())
	}
}

// loop over all targets
func checkAllUrls(urls []string) {
	urlChannel := make(chan urlStatus)
	for _, url := range urls {
		go checkUrl(url, urlChannel)
	}
	result := make([]urlStatus, len(urls))
	for i, _ := range result {
		result[i] = <-urlChannel
		if result[i].status {
			// report success only if not in quiet mode
			if !config.Quiet || config.Interval < 0 {
				log.Printf("💖 %s %s", result[i].url, "is up.")
			}
		} else {
			log.Printf("⚡ %s %s", result[i].url, "is down !!")
		}
	}
}

// checks and prints a message if a website is up or down
func checkUrl(url string, c chan urlStatus) {
	start := time.Now()

	// https://stackoverflow.com/a/25344458/4292075
	// How to set timeout for http.Get() requests in Golang?
	client := http.Client{
		Timeout: config.Timeout,
	}
	_, err := client.Get(url)

	elapsed := time.Since(start)
	var checkResult = new(CheckResult)
	checkResult.target = url
	checkResult.responseTime = elapsed
	checkResult.checkTime = time.Now()
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
	healthStatus.Results = append(healthStatus.Results, *checkResult)
	for idx, _ := range healthStatus.Results {
		if idx >= config.Histlen {
			healthStatus.Results = healthStatus.Results[1:] // Dequeue
		}
	}
}

// Healthbell's own healthcheck, returns JSON
func health(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	status, err := json.Marshal(map[string]interface{}{
		"status": "up",
		"info":   fmt.Sprintf("%s is healthy", appid),
		"time":   time.Now().Format(time.RFC3339),
	})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Write(status)
}

// Stop the check loop
func suspend(w http.ResponseWriter, req *http.Request) {
	log.Printf("Suspending checkloop")
	close(quitChanel)
}

// Nice status reponse for humans
func status(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	// https://purecss.io/start/
	fmt.Fprintf(w, `<html>
<head>
<link rel='stylesheet' href='https://unpkg.com/purecss@2.0.3/build/pure-min.css' crossorigin='anonymous'></link>
</head>
<body>
<table class='pure-table pure-table-horizontal'>
<thead>
<tr><th>Target</th><th>Checktime</th><th>Healthy</th><th>Time2respond</th></tr>
</thead>
<tbody>`)
	//now := time.Now()
	for i := len(healthStatus.Results) - 1; i >= 0; i-- {
		element := healthStatus.Results[i]
		fmt.Fprintf(w, "\n  <tr><td><a href='%s' target='_blank'>%s</a></td><td>%s</td><td>%v</td><td>%v</td></tr>",
			element.target,
			element.target,
			// element.checkTime.Format(time.RFC3339),
			humanize.Time(element.checkTime),
			element.healthy,
			element.responseTime.Round(time.Millisecond),
		)
	}
	fmt.Fprintf(w, `
</tbody>
</table>
</body>
</html>
`)
}
