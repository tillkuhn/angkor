package main

import (
	"log"
	"os"
	"path/filepath"

	"github.com/disintegration/imaging"
	"github.com/rwcarlsen/goexif/exif"
)

func extractExif(filename string) (map[string]string, error) {
	tagmap := make(map[string]string)

	log.Printf("Anlyzing exif data for image %v", filename)
	imgFileExif, errExifOpen := os.Open(filename)
	if errExifOpen != nil {
		log.Printf("ERROR openExif %v", errExifOpen.Error())
		return tagmap, errExifOpen
	}
	metaData, exifErrDecode := exif.Decode(imgFileExif)
	if exifErrDecode != nil {
		log.Printf("ERROR exifErrDecode %v", exifErrDecode.Error())
		return tagmap, exifErrDecode
	}
	dateTimeOrig, _ := metaData.Get(exif.DateTimeOriginal)
	if dateTimeOrig != nil {
		tagmap["dateTimeOriginal"] = dateTimeOrig.String()
	}
	pixelx, _ := metaData.Get(exif.PixelXDimension)
	pixely, _ := metaData.Get(exif.PixelYDimension)
	if pixelx != nil && pixely != nil {
		tagmap["dimensions"] = pixelx.String() + "x" + pixely.String()
	}
	lm, _ := metaData.Get(exif.LensModel)
	if lm != nil {
		tagmap["lensModel"] = lm.String()
	}
	return tagmap, nil
}

func createThumbnail(filename string) string {

	src, err := imaging.Open(filename)
	if err != nil {
		log.Fatalf("failed to open image %s: %v", filename, err)
	}

	log.Printf("Anlyzing image %v", filename)
	imgFileExif, errExifOpen := os.Open(filename)
	if errExifOpen != nil {
		log.Fatal(errExifOpen.Error())
	}

	metaData, exifErrDecode := exif.Decode(imgFileExif)
	if exifErrDecode != nil {
		log.Fatal(exifErrDecode.Error())
	}

	//camModel, _ := metaData.Get(exif.Model) // normally, don't ignore errors!
	dateTimeOrig, _ := metaData.Get(exif.DateTimeOriginal)
	if dateTimeOrig != nil {
		dateTimeOrigStr, _ := dateTimeOrig.StringVal()
		log.Printf("Image Taken:" + dateTimeOrigStr)
	} else {
		log.Printf("No dateTimeOrig exif info")
	}
	// Resize the cropped image to width = 1200px preserving the aspect ratio.
	thumbnail := imaging.Resize(src, 1200, 0, imaging.Lanczos)

	// Save the resulting image as JPEG.
	var extension = filepath.Ext(filename)
	var thumbnailFile = (filename)[0:len(filename)-len(extension)] + "_mini.jpg"
	log.Printf("Convert %s to thumbnail %s", filename, thumbnailFile)
	err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(80))
	if err != nil {
		log.Fatalf("failed to save image: %v", err)
	}
	return thumbnailFile

	/*
		jsonByte, jsonErr := metaData.MarshalJSON()
		if jsonErr != nil {
			log.Fatal(jsonErr.Error())
		}
		fmt.Println(string(jsonByte))
	*/
}
