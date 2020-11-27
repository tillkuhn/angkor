package main

import (
	"strings"
	"testing"
)

func TestMemStats(t *testing.T) {
	result := memStats()
	if !strings.Contains(result, "TotalAlloc") {
		t.Errorf("TestMemStats() = %v; expect to contain %v", result, "TotalAlloc")
	}
}
