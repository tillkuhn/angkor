package main

import (
    "encoding/json"
    "fmt"
    "log"
    "net/http"
    "os"
    "sync"
    "time"

    // "github.com/tillkuhn/angkor/go/topkapi"

    "github.com/dustin/go-humanize"
    "github.com/kelseyhightower/envconfig"
)

// used as envconfig prefix and as a unique identity of this service, e.g., for health checking

// Config used to configure the app via https://github.com/kelseyhightower/envconfig
type Config struct {
    Quiet        bool          `default:"true"` // e.g. HEALTHBELLS_DEBUG=true
    Port         int           `default:"8091"`
    Interval     time.Duration `default:"-1ms"` // e.g. HEALTHBELLS_INTERVAL=5s
    Timeout      time.Duration `default:"10s"`  // e.g. HEALTHBELLS_TIMEOUT=10s
    Urls         []string      `default:"https://www.timafe.net/,https://timafe.wordpress.com/"`
    Histlen      int           `default:"25"` // how many items to keep ...
    KafkaSupport bool          `default:"true" desc:"Send important events to Kafka Topic(s)" split_words:"true"`
}

type urlStatus struct {
    url        string
    status     bool
    statusCode int
}

type CheckResult struct {
    target       string
    healthy      bool
    responseTime time.Duration
    checkTime    time.Time
    statusCode   int
}

// HealthStatus keeps current and previous records see
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
    BuildTime = "latest"

    AppId  = "healthbells"
    logger = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)
    // kafkaClient *topkapi.Client
)

// Let's rock ...
func main() {
    startMsg := fmt.Sprintf("Starting service [%s] build %s with PID %d", AppId, BuildTime, os.Getpid())
    logger.Printf(startMsg)

    err := envconfig.Process(AppId, &config)
    // todo explain envconfig.Usage()
    if err != nil {
        logger.Fatal(err.Error())
    }
    logger.Printf("Quiet %v Port %d Interval %v Timeout %v", config.Quiet, config.Port, config.Interval, config.Timeout)

    // Kafka event support
    /*
    	kafkaClient = topkapi.NewClientWithId(AppId)
    	kafkaClient.Enable(config.KafkaSupport)
    	defer kafkaClient.Close()
    	if _, _, err := kafkaClient.PublishEvent(kafkaClient.NewEvent("startup:"+AppId, startMsg), "system"); err != nil {
    		logger.Printf("Error publish event to %s: %v", "system", err)
    	}
    */
    logger.Printf("Kafka Even Support disabled")

    // Let's check all URLs ...
    logger.Printf("Initial run for %v", config.Urls)
    checkAllUrls(config.Urls) // check only once
    if config.Interval >= 0 {
        logger.Printf("Setting up timer check interval=%v", config.Interval)
        ticker := time.NewTicker(config.Interval)
        go func() {
            for {
                select {
                case <-ticker.C:
                    checkAllUrls(config.Urls)
                case <-quitChanel:
                    ticker.Stop()
                    logger.Printf("Check Loop stopped")
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
        logger.Printf("Starting HTTP Server on http://localhost:%d", config.Port)
        logger.Fatal(srv.ListenAndServe())
    }
}

// loop over all targets
func checkAllUrls(urls []string) {
    urlChannel := make(chan urlStatus)
    for _, url := range urls {
        go checkUrl(url, urlChannel)
    }
    result := make([]urlStatus, len(urls))
    for i := range result {
        result[i] = <-urlChannel
        if result[i].status {
            // report success only if not in quiet mode
            if !config.Quiet || config.Interval < 0 {
                logger.Printf("💖 %s is up with status=%d", result[i].url, result[i].statusCode)
            }
        } else {
            msg := fmt.Sprintf("⚡ %s is down with status=%d", result[i].url, result[i].statusCode)
            /*
            	if _, _, kerr := kafkaClient.PublishEvent(kafkaClient.NewEvent("alert:unavailable", msg), "system"); kerr != nil {
            		logger.Println(kerr.Error())
            	}*/
            logger.Println(msg)
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
    response, err := client.Get(url)
    elapsed := time.Since(start)
    var statusCode = 0
    if response != nil {
        statusCode = response.StatusCode
    }
    checkResult := &CheckResult{
        target:       url,
        responseTime: elapsed,
        checkTime:    time.Now(),
        statusCode:   statusCode,
    }
    if err != nil || (statusCode < 200 || statusCode >= 300) {
        // The website is down
        c <- urlStatus{url, false, statusCode}
        checkResult.healthy = false
    } else {
        // The website is up
        c <- urlStatus{url, true, statusCode}
        checkResult.healthy = true
    }
    healthStatus.Lock()
    defer healthStatus.Unlock()
    healthStatus.Results = append(healthStatus.Results, *checkResult)
    for idx := range healthStatus.Results {
        if idx >= config.Histlen {
            healthStatus.Results = healthStatus.Results[1:] // Dequeue
        }
    }
}

// our own health check, returns JSON
func health(w http.ResponseWriter, _ *http.Request) {
    w.Header().Set("Content-Type", "application/json")
    status, err := json.Marshal(map[string]interface{}{
        "status": "up",
        "info":   fmt.Sprintf("%s is healthy", AppId),
        "time":   time.Now().Format(time.RFC3339),
    })
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
    _, _ = w.Write(status)
}

// Stop the check loop
func suspend(http.ResponseWriter, *http.Request) {
    logger.Printf("Suspending check loop")
    close(quitChanel)
}

// produce status response for humans
func status(w http.ResponseWriter, _ *http.Request) {
    w.Header().Set("Content-Type", "text/html; charset=utf-8")
    // https://purecss.io/start/
    _, _ = fmt.Fprintf(w, `<html>
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
        _, _ = fmt.Fprintf(w, "\n  <tr><td><a href='%s' target='_blank'>%s</a></td><td>%s</td><td>%v</td><td>%v</td></tr>",
            element.target,
            element.target,
            // element.checkTime.Format(time.RFC3339),
            humanize.Time(element.checkTime),
            element.healthy,
            element.responseTime.Round(time.Millisecond),
        )
    }
    _, _ = fmt.Fprintf(w, `
</tbody>
</table>
</body>
</html>
`)
}
