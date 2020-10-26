package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"path/filepath"

	"github.com/disintegration/imaging"
	"github.com/rwcarlsen/goexif/exif"
)

func main() {
	// Open a test image.
	// usr, _ := user.Current() //  usr.HomeDir + "/tmp/elba.jpg",
	filename := flag.String("file", "","relative or absolute file location to \"imagine\"")
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
	/*
	jsonByte, jsonErr := metaData.MarshalJSON()
	if jsonErr != nil {
		log.Fatal(jsonErr.Error())
	}
	fmt.Println(string(jsonByte))
	 */
	//camModel, _ := metaData.Get(exif.Model) // normally, don't ignore errors!
	dateTimeOrig,_ := metaData.Get(exif.DateTimeOriginal)
	dateTimeOrigStr,_ := dateTimeOrig.StringVal()
	fmt.Println("Taken:" + dateTimeOrigStr)
	// Resize the cropped image to width = 1200px preserving the aspect ratio.
	thumbnail := imaging.Resize(src, 1200, 0, imaging.Lanczos)

	/*
	// Crop the original image to 300x300px size using the center anchor.
	src = imaging.CropAnchor(src, 300, 300, imaging.Center)

	// Resize the cropped image to width = 200px preserving the aspect ratio.
	src = imaging.Resize(src, 200, 0, imaging.Lanczos)

	// Create a blurred version of the image.
	img1 := imaging.Blur(src, 5)

	// Create a grayscale version of the image with higher contrast and sharpness.
	img2 := imaging.Grayscale(src)
	img2 = imaging.AdjustContrast(img2, 20)
	img2 = imaging.Sharpen(img2, 2)

	// Create an inverted version of the image.
	img3 := imaging.Invert(src)

	// Create an embossed version of the image using a convolution filter.
	img4 := imaging.Convolve3x3(
		src,
		[9]float64{
			-1, -1, 0,
			-1, 1, 1,
			0, 1, 1,
		},
		nil,
	)

	// Create a new image and paste the four produced images into it.
	dst := imaging.New(400, 400, color.NRGBA{0, 0, 0, 0})
	dst = imaging.Paste(dst, img1, image.Pt(0, 0))
	dst = imaging.Paste(dst, img2, image.Pt(0, 200))
	dst = imaging.Paste(dst, img3, image.Pt(200, 0))
	dst = imaging.Paste(dst, img4, image.Pt(200, 200))
	 */

	// Save the resulting image as JPEG.
	var extension = filepath.Ext(*filename)
	var thumbnailFile = (*filename)[0:len(*filename)-len(extension)] + "_mini.jpg"
	log.Printf("Convert %s to thumbnail %s",*filename,thumbnailFile)
	err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(80))
	if err != nil {
		log.Fatalf("failed to save image: %v", err)
	}
}
