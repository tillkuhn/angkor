package main
// based on https://medium.com/@gauravsingharoy/asynchronous-programming-with-go-546b96cd50c1

import (
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

type checkResult struct {
	healthy bool
	responseTime time.Duration
	checkTime time.Time
}
// https://stackoverflow.com/questions/17890830/golang-shared-communication-in-async-http-server/17930344
// Todo use some stack
type results struct {
	*sync.Mutex // inherits locking methods
	Results map[string]checkResult // map ids to values
}


var CurrentResults= &results{&sync.Mutex{}, map[string]checkResult{}}


// see https://github.com/kelseyhightower/envconfig
type Config struct {
	Debug       bool   // HEALTHBELLS_DEBUG=true will see, default is implicitly false as it's a boolean
	Port        int `default:"8092"`
	IntervalSeconds int `default:"-1"`
	Urls []string `default:"https://www.timafe.net/,https://timafe.wordpress.com/"`
	//User        string
	//Users       []string
	//Rate        float32
	//Timeout     time.Duration
	//ColorCodes  map[string]int
}
func main() {
	var config Config
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatal(err.Error())
	}
	log.Printf("Debug %v Port %d IntervalSeconds %d",config.Debug,config.Port,config.IntervalSeconds)

	if (config.IntervalSeconds >= 0) {
		log.Printf("Setup timer for internval %d seconds",config.IntervalSeconds)
		ticker := time.NewTicker(time.Duration(config.IntervalSeconds) * time.Second)
		quit := make(chan struct{})
		go func() {
			for {
				select {
				case <- ticker.C:
					checkUrlList(config.Urls)
				case <- quit:
					ticker.Stop()
					return
				}
			}
		}()
		log.Printf("Running HTTP Server Listen on :%d",config.Port)
		http.HandleFunc("/", status)
		srv := &http.Server{
			Addr:         fmt.Sprintf(":%d",config.Port),
			WriteTimeout: 15 * time.Second,
			ReadTimeout:  15 * time.Second,
		}

		log.Fatal(srv.ListenAndServe())
	} else {
		checkUrlList(config.Urls) // check onlny once
	}

}

func status(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	fmt.Fprintf(w,"<ul>")
	for key, element := range CurrentResults.Results {
		fmt.Fprintf(w, "<li>Status %s: checktime=%s healthy=%v responseTime=%v</li>",
			key,element.checkTime.Format(time.RFC3339),
			element.healthy,element.responseTime)
	}
	fmt.Fprintf(w,"</ul>")
}

func checkUrlList(urls []string) {
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
	var checkResult= new(checkResult)
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
	CurrentResults.Lock()
	defer CurrentResults.Unlock()
	CurrentResults.Results[url]=*checkResult
}
