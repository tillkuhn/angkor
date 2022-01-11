package image

import (
	"github.com/tillkuhn/angkor/tools/imagine/utils"
	"os"
	"testing"
)

const testDir = "../static"

func TestAddTagJPEG(t *testing.T) {
	tagMap, err := ExtractExif(testDir + "/test-image.jpg")
	if err != nil {
		t.Errorf("ExtractExif: %v", err)
	}
	// log.Print(tagMap)
	if tagMap["ExposureTime"] != "\"1/400\"" {
		t.Errorf("Expected ExposureTime to contain 1/400 got %v", tagMap["ExposureTime"])
	}
}

func TestResizeJPG(t *testing.T) {
	testResize("jpg", t)
}

func TestResizePNG(t *testing.T) {
	testResize("png", t)
}

func testResize(ext string, t *testing.T) {

	// Remember to clean up the file afterwards
	filename := "../static/test-image." + ext
	if utils.IsResizableImage(filename) {
		t.Errorf("%s is not considered to be an image", filename)
	}
	thumb1 := testDir + "/test-image_150." + ext
	thumb2 := testDir + "/test-image_300." + ext
	defer os.Remove(thumb1)
	defer os.Remove(thumb2)
	resizeModes := make(map[string]int)
	resizeModes["small"] = 150
	resizeModes["medium"] = 300
	resizeResponse := ResizeImage(filename, resizeModes, 80)
	if resizeResponse == nil || len(resizeResponse) < 2 {
		t.Errorf("Expected resizeResponse for %s len 2, got %v", filename, resizeResponse)
	}
	if fileNotExists(thumb1) || fileNotExists(thumb2) {
		t.Errorf("at least one thumb was not created %s %s", thumb1, thumb2)
	}
}

func fileNotExists(path string) bool {
	_, err := os.Stat(path)
	return os.IsNotExist(err)
}
