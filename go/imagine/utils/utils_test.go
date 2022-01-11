package utils

import (
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestMp3(t *testing.T) {
	assert.Equal(t, true, IsMP3("audio/mpeg"))
}

func TestJPEG(t *testing.T) {
	assert.Equal(t, true, IsJPEG("image/jpeg"))
}

func TestFalse(t *testing.T) {
	assert.False(t, IsJPEG("image/nothing"))
	assert.False(t, IsMP3("image/nothing"))
}

func TestHasExtension(t *testing.T) {
	assert.True(t, HasExtension("hase.mp3"))
	assert.False(t, HasExtension("hi-there"))
}

func TestFileSize(t *testing.T) {
	f, err := os.Open("../go.mod")
	assert.NoError(t, err)
	fs := FileSize(f)
	assert.Greater(t, fs, int64(0))
}

func TestFileSizeNotExists(t *testing.T) {
	f, _ := os.Open("not.exists")
	fs := FileSize(f)
	assert.Equal(t, fs, int64(-1))
}

func TestStripRequestParams(t *testing.T) {
	check := "https://hase.com/1.jpg?horst=xxx"
	expect := "https://hase.com/1.jpg"
	actual := StripRequestParams(check)
	if expect != actual {
		t.Errorf("TestStripRequestParams() expected %v but got %v", expect, actual)
	}
	// Test double encode protection
	check = "https://test.com/Sehensw%C3%BCrdigkeiten-und-Tipps-f%C3%BCr-Visby-78.jpg"
	expect = check
	actual = StripRequestParams(check)
	if expect != actual {
		t.Errorf("TestStripRequestParams() expected %v but got %v", expect, actual)
	}

}
