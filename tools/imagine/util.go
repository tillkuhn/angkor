package main

import (
	"fmt"
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
		log.Printf("WARNIG Cannot obtain filesize: %v", err)
		return -1
	}
	return fStat.Size()
}

func MemStats() string {
	var m runtime.MemStats
	runtime.ReadMemStats(&m)
	return fmt.Sprintf("Alloc=%s TotalAlloc=%s HeapReleased=%s NumGC = %v",
		humanize.Bytes(m.Alloc), humanize.Bytes(m.TotalAlloc), humanize.Bytes(m.HeapReleased), m.NumGC)
}

// url could contain request params e.g. bla.jpg?v=333, so we need to slice them away
func StripRequestParams(url string) string {
	if strings.Contains(url, "?") {
		return url[:strings.IndexByte(url, '?')]
	}
	return url
}

func HasExtension(filename string) bool {
	return strings.Contains(filename, ".")
}
