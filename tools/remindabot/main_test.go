package main

import (
	"strings"
	"testing"
	"time"
)

func TestAddTagJPEG(t *testing.T) {
	actual := mailSubject()
	wd := time.Now().Weekday().String()
	if !strings.Contains(actual, wd) {
		t.Errorf("Subject test failed expected '%s' to contain '%s'", actual, wd)
	}
}
