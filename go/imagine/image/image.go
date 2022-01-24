package image

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/tillkuhn/angkor/tools/imagine/utils"

	"github.com/disintegration/imaging"
	"github.com/rs/zerolog/log"
	"github.com/rwcarlsen/goexif/exif"
)

// ExtractExif extracts EXIF data from image and puts it into a simple map
// Suitable for S3 Object Tags
func ExtractExif(filename string) (map[string]string, error) {
	imgLogger := log.Logger.With().Str("logger", "🎨image").Logger()

	tagMap := make(map[string]string)
	imgLogger.Printf("Analyzing exif data for image %v", filename)
	imgFileExif, errExifOpen := os.Open(filename)
	defer utils.CheckedClose(imgFileExif)

	if errExifOpen != nil {
		imgLogger.Printf("ERROR openExif %v", errExifOpen.Error())
		return tagMap, errExifOpen
	}
	metaData, exifErrDecode := exif.Decode(imgFileExif)
	if exifErrDecode != nil {
		imgLogger.Printf("ERROR exifErrDecode %v", exifErrDecode.Error())
		return tagMap, exifErrDecode
	}
	addTag(metaData, tagMap, exif.DateTimeOriginal)
	addTag(metaData, tagMap, exif.PixelXDimension)
	addTag(metaData, tagMap, exif.PixelYDimension)
	addTag(metaData, tagMap, exif.LensModel)
	addTag(metaData, tagMap, exif.FNumber)
	addTag(metaData, tagMap, exif.ExposureTime)
	addTag(metaData, tagMap, exif.ISOSpeedRatings)
	return tagMap, nil
}

func addTag(meta *exif.Exif, tagMap map[string]string, field exif.FieldName) {
	tagValue, err := meta.Get(field)
	if err != nil {
		log.Error().Msgf("Error cannot get %s: %v", field, err)
		return
	}
	tagMap[string(field)] = tagValue.String()
}

// ResizeImage resizes versions of the image and return array of temporary location
// this is hopefully more efficient in terms of memory management since we need to open image ony once
func ResizeImage(filename string, resizeModes map[string]int, resizeQuality int) map[string]string {
	resizeResponse := make(map[string]string)

	src, err := imaging.Open(filename)
	if err != nil {
		log.Printf("ERROR failed to open image to resize %s: %v", filename, err)
		return resizeResponse
	}

	for resizeMode, size := range resizeModes {

		// Resize the cropped image to width = 12345 px preserving the aspect ratio.
		thumbnail := imaging.Resize(src, size, 0, imaging.Lanczos)

		// Save the resulting image as JPEG.
		extension := filepath.Ext(filename)
		var thumbnailFile = fmt.Sprintf("%s_%d%s", (filename)[0:len(filename)-len(extension)], size, extension)
		log.Printf("Convert %s to temporary thumbnail %s quality %d", filename, thumbnailFile, resizeQuality)
		err = imaging.Save(thumbnail, thumbnailFile, imaging.JPEGQuality(resizeQuality))
		if err != nil {
			log.Error().Msgf("ERROR failed to create resize image %s: %v, skipping", thumbnailFile, err)
			thumbnailFile = ""
		}
		resizeResponse[resizeMode] = thumbnailFile
	}
	return resizeResponse
}
