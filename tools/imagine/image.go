package main

import (
	"fmt"
	"log"
	"os"
	"path/filepath"

	"github.com/disintegration/imaging"
	"github.com/rwcarlsen/goexif/exif"
)

func ExtractExif(filename string) (map[string]string, error) {
	tagmap := make(map[string]string)

	log.Printf("Anlyzing exif data for image %v", filename)
	imgFileExif, errExifOpen := os.Open(filename)
	defer imgFileExif.Close()

	if errExifOpen != nil {
		log.Printf("ERROR openExif %v", errExifOpen.Error())
		return tagmap, errExifOpen
	}
	metaData, exifErrDecode := exif.Decode(imgFileExif)
	if exifErrDecode != nil {
		log.Printf("ERROR exifErrDecode %v", exifErrDecode.Error())
		return tagmap, exifErrDecode
	}
	addTag(metaData, tagmap, exif.DateTimeOriginal)
	addTag(metaData, tagmap, exif.PixelXDimension)
	addTag(metaData, tagmap, exif.PixelYDimension)
	addTag(metaData, tagmap, exif.LensModel)
	addTag(metaData, tagmap, exif.FNumber)
	addTag(metaData, tagmap, exif.ExposureTime)
	addTag(metaData, tagmap, exif.ISOSpeedRatings)
	return tagmap, nil
}

func addTag(meta *exif.Exif, tagmap map[string]string, field exif.FieldName) {
	tagval, err := meta.Get(field)
	if err != nil {
		if config.Debug {
			log.Printf("Error cannot get %s: %v", field, err)
		}
		return
	}
	tagmap[string(field)] = tagval.String()
}

// create a resized version of the image
// and return the temporary location
func ResizeImage(filename string, resizeWidth int) string {

	src, err := imaging.Open(filename)
	if err != nil {
		log.Printf("ERROR failed to open image to resize %s: %v", filename, err)
		return ""
	}

	// Resize the cropped image to width = xxxx px preserving the aspect ratio.
	thumbnail := imaging.Resize(src, resizeWidth, 0, imaging.Lanczos)

	// Save the resulting image as JPEG.
	extension := filepath.Ext(filename)
	var thumbnailFile = fmt.Sprintf("%s_%d%s", (filename)[0:len(filename)-len(extension)], resizeWidth, extension)
	log.Printf("Convert %s to temporary thumbnail %s qual %d", filename, thumbnailFile, config.ResizeQuality)
	err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(config.ResizeQuality))
	if err != nil {
		log.Printf("ERROR failed to create resize image %s: %v", thumbnailFile, err)
		return ""
	}
	return thumbnailFile

}
