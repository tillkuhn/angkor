package main

import (
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
