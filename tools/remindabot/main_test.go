package main

import (
	"strconv"
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

func TestFooter(t *testing.T) {
	actual := mailFooter()
	year := time.Now().Year()
	rel := "Latest"
	if !strings.Contains(actual, rel) || !strings.Contains(actual, strconv.Itoa(year)) {
		t.Errorf("Subject test failed expected '%s' to contain '%d' and '%s'", actual, year, rel)
	}
}
