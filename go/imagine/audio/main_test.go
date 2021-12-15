package audio

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

// TestReadMp3, credits to John Bartmann for putting the test song under a free public license
func TestReadMp3(t *testing.T) {
	tags, err := ExtractTags("../static/test-song.mp3")
	assert.NoError(t, err)
	assert.Equal(t, "Secret Agent Rock", tags["Title"])
	assert.Equal(t, "John Bartmann", tags["Artist"])
}
