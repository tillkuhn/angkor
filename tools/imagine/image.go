package main

import (
	"github.com/disintegration/imaging"
	"github.com/rwcarlsen/goexif/exif"
	"log"
	"os"
	"path/filepath"
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
	addTag(metaData,tagmap,exif.DateTimeOriginal)
	addTag(metaData,tagmap,exif.PixelXDimension)
	addTag(metaData,tagmap,exif.PixelYDimension)
	addTag(metaData,tagmap,exif.LensModel)
	addTag(metaData,tagmap,exif.FNumber)
	addTag(metaData,tagmap,exif.ExposureTime)
	addTag(metaData,tagmap,exif.ISOSpeedRatings)
	return tagmap, nil
}

func addTag(meta *exif.Exif,tagmap map[string]string,field exif.FieldName) {
	tagval, err := meta.Get(field)
	if err != nil {
		log.Printf("Error cannot get %s: %v",field,err)
		return
	}
	tagmap[string(field)] = tagval.String()
}
func CreateThumbnail(filename string) string {

	src, err := imaging.Open(filename)
	if err != nil {
		log.Printf("ERROR failed to open image %s: %v", filename, err)
		return ""
	}

	// Resize the cropped image to width = xxxx px preserving the aspect ratio.
	thumbnail := imaging.Resize(src, config.Thumbsize, 0, imaging.Lanczos)

	// Save the resulting image as JPEG.
	extension := filepath.Ext(filename)
	var thumbnailFile = (filename)[0:len(filename)-len(extension)] + "_thumb.jpg"
	log.Printf("Convert %s to temporary thumbnail %s", filename, thumbnailFile)
	err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(config.Thumbquality))
	if err != nil {
		log.Printf("ERROR failed to create thumbnail image: %v", err)
		return ""
	}
	return thumbnailFile

}
