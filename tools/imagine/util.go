package main

import (
	"fmt"
	"github.com/dustin/go-humanize"
	"log"
	"os"
	"runtime"
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
