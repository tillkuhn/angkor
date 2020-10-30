package main

import (
	"github.com/gorilla/mux"
	"log"
	"net/http"
	"os"
	"time"
)

const (
	S3_REGION = "eu-central-1"
	S3_BUCKET = "timafe-angkor-data-dev"
	PORT = "8090"
	// S3_ACL    = "public-read"
)
func main() {
	// Open a test image.
	// usr, _ := user.Current() //  usr.HomeDir + "/tmp/elba.jpg",
	// filename := flag.String("file", "", "relative or absolute file location to \"imagine\"")
	// flag.Parse()
	//if *filename == "" {
	//	flag.PrintDefaults()
	//	os.Exit(1)
	//}

	router := mux.NewRouter()
	//router.HandleFunc("/", testform).Methods("GET")
	router.HandleFunc("/upload/{entityType}/{entityId}", uploadToTmp).Methods("POST")
	router.HandleFunc("/api", apiGet).Methods("GET")
	router.HandleFunc("/health", HealthCheckHandler)
	_, errStatDir := os.Stat("./static")
	if os.IsNotExist(errStatDir) {
		log.Printf("No Static dir /static")
	} else {
		log.Printf("Setting up route to local /static")
		router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static")))
	}
	log.Printf("HTTP Server Listen on http://localhost:%s",PORT)
	srv := &http.Server{
		Handler:      router,
		Addr:         ":"+PORT,
		WriteTimeout: 15 * time.Second,
		ReadTimeout:  15 * time.Second,
	}

	log.Fatal(srv.ListenAndServe())
}

