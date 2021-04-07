package main

import (
	"github.com/rs/xid"
	"strings"
	"testing"
)

func TestMemStats(t *testing.T) {
	result := MemStats()
	if !strings.Contains(result, "TotalAlloc") {
		t.Errorf("TestMemStats() = %v; expect to contain %v", result, "TotalAlloc")
	}
}

func TestXID(t *testing.T) {
	result := xid.New().String() // e.g. c1m8eof2hran66ddldlg
	if len(result) <= 8 {
		t.Errorf("TextXID() %v invalid length should be min 8", result)
	}
}
