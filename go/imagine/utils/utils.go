package utils

import (
	"fmt"
	"io"
	"log"
	"os"
	"runtime"
	"strings"

	"github.com/dustin/go-humanize"
)

func FileSize(file *os.File) int64 {
	fStat, err := file.Stat()
	if err != nil {
		// Could not obtain stat, handle error
		log.Printf("WARNIG Cannot obtain filesize of %v: %v", file, err)
		return -1
	}
	return fStat.Size()
}

// StripRequestParams covers cases where url contains request params e.g. bla.jpg?v=333, so we need to slice them away
func StripRequestParams(url string) string {
	if strings.Contains(url, "?") {
		return url[:strings.IndexByte(url, '?')]
	}
	return url
}

func HasExtension(filename string) bool {
	return strings.Contains(filename, ".")
}

func IsResizableImage(contentType string) bool {
	return contentType == "image/jpeg" || contentType == "image/png"
}

// IsJPEG checks if the content qualifies as JPEG image, since only JPEG supports EXIF
func IsJPEG(contentType string) bool {
	return contentType == "image/jpeg"
}

// IsMP3 checks if the content qualifies as MP3, since we currently offer only MP3 support for tags
func IsMP3(contentType string) bool {
	return contentType == "audio/mpeg"
}

// CheckedClose can be used in defer statements to defer close() operation silently w/o need to check for errors
func CheckedClose(c io.Closer) {
	if err := c.Close(); err != nil {
		fmt.Println(err)
	}
}

func MemStats() string {
	var m runtime.MemStats
	runtime.ReadMemStats(&m)
	return fmt.Sprintf("Alloc=%s TotalAlloc=%s HeapReleased=%s NumGC = %v",
		humanize.Bytes(m.Alloc), humanize.Bytes(m.TotalAlloc), humanize.Bytes(m.HeapReleased), m.NumGC)
}
