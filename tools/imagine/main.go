package main

import (
	"flag"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/gorilla/mux"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/disintegration/imaging"
	"github.com/rwcarlsen/goexif/exif"
)

const (
	S3_REGION = "eu-central-1"
	S3_BUCKET = "timafe-angkor-data-dev"
	PORT = "8090"
	// S3_ACL    = "public-read"
)
func main() {
	router := mux.NewRouter()
	//router.PathPrefix("/").Handler(http.FileServer(http.Dir("./static/")))
	router.HandleFunc("/", testform).Methods("GET")
	router.HandleFunc("/upload/{entityType}/{entityId}", upload).Methods("POST")
	router.HandleFunc("/api", apiGet).Methods("GET")
	log.Printf("HTTP Server Listen on http://localhost:%s",PORT)
	srv := &http.Server{
		Handler:      router,
		Addr:         ":"+PORT,
		WriteTimeout: 15 * time.Second,
		ReadTimeout:  15 * time.Second,
	}

	log.Fatal(srv.ListenAndServe())
}

func main2() {
	// Open a test image.
	// usr, _ := user.Current() //  usr.HomeDir + "/tmp/elba.jpg",
	filename := flag.String("file", "", "relative or absolute file location to \"imagine\"")
	flag.Parse()
	if *filename == "" {
		flag.PrintDefaults()
		os.Exit(1)
	}
	src, err := imaging.Open(*filename)
	if err != nil {
		log.Fatalf("failed to open image %s: %v", *filename, err)
	}

	imgFileExif, errExifOpen := os.Open(*filename)
	if errExifOpen != nil {
		log.Fatal(errExifOpen.Error())
	}

	metaData, exifErrDecode := exif.Decode(imgFileExif)
	if exifErrDecode != nil {
		log.Fatal(exifErrDecode.Error())
	}

	//camModel, _ := metaData.Get(exif.Model) // normally, don't ignore errors!
	dateTimeOrig, _ := metaData.Get(exif.DateTimeOriginal)
	dateTimeOrigStr, _ := dateTimeOrig.StringVal()
	fmt.Println("Taken:" + dateTimeOrigStr)
	// Resize the cropped image to width = 1200px preserving the aspect ratio.
	thumbnail := imaging.Resize(src, 1200, 0, imaging.Lanczos)


	// Save the resulting image as JPEG.
	var extension = filepath.Ext(*filename)
	var thumbnailFile = (*filename)[0:len(*filename)-len(extension)] + "_mini.jpg"
	log.Printf("Convert %s to thumbnail %s", *filename, thumbnailFile)
	err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(80))
	if err != nil {
		log.Fatalf("failed to save image: %v", err)
	}

	sess, err := session.NewSession(&aws.Config{Region: aws.String(S3_REGION)})
	if err != nil {
		log.Fatalf("session.NewSession - filename: %v, err: %v", filename, err)
	}

	handler := S3Handler{
		Session: sess,
		Bucket:  S3_BUCKET,
	}

	// contents, err := handler.ReadFile(filename)
	// if err != nil {
	// 	log.Fatalf("ReadFile - filename: %v, err: %v", filename, err)
	// }

	err = handler.UploadFile("/tmp/"+filepath.Base(*filename), *filename)
	if err != nil {
		log.Fatalf("UploadFile - filename: %v, err: %v", *filename, err)
	}
	log.Printf("UploadFile - success")

	/*
		jsonByte, jsonErr := metaData.MarshalJSON()
		if jsonErr != nil {
			log.Fatal(jsonErr.Error())
		}
		fmt.Println(string(jsonByte))
	*/
}
