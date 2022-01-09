package audio

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

// TestReadMp3, credits to John Bartmann for putting the test song under a free public license
func TestReadMp3(t *testing.T) {
	song := "test-song.mp3"
	tags, err := ExtractTags("../static/" + song)
	assert.NoError(t, err)
	assert.Equal(t, "Secret Agent Rock", tags["Title"])
	assert.Equal(t, "John Bartmann", tags["Artist"])
}

func TestDoNotAddEmpty(t *testing.T) {
	tagMap := make(map[string]string)
	tagMap["ContentType"] = "some/thing"
	addTagIfNotEmpty(tagMap, "e1", " ")
	addTagIfNotEmpty(tagMap, "e2", "")
	addTagIfNotEmpty(tagMap, "c", "content")
	assert.Equal(t, 2, len(tagMap))
}

// Example Raw Content for tags:
// for k, v := range meta.Raw() {
// 	fmt.Printf("  %s: %s\n", k, v)
// }
//
// TPE1: Kruder & Dorfmeister
// TYER: 2020
// TCOP: G-Stone Recordings
// PRIV: www.amazon.com
// TIT2: King Size
// TCON: Dance & DJ
// TPE3:
// TRCK: 6/14
// TALB: 1995
// TPE2: Kruder & Dorfmeister
// TPOS: 1/1
// APIC: Picture{Ext: jpg, MIMEType: image/jpeg, Type: Cover (front), Description: , Data.Size: 11435}
// TCOM: Peter Kruder
// COMM: Text{Lang: 'eng', Description: '', 0 lines}
