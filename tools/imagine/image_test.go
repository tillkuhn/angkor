package main

import (
	"testing"
)

func TestAddTag(t *testing.T) {
	tagmap, err := ExtractExif("static/testimage.jpg")
	if err != nil {
		t.Errorf("ExtractExif: %v",err)
	}
	// log.Print(tagmap)
	if tagmap["ExposureTime"] != "\"1/400\"" {
		t.Errorf("Expected ExposureTime to contain 1/400 got %v", tagmap["ExposureTime"])
	}
}
