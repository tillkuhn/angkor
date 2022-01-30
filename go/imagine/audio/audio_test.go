package audio

import (
	"github.com/dhowden/tag"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
)

func init() {
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stdout})
}

// TestReadMp3, credits to John Bartmann for putting the test song under a free public license
func TestReadMp3(t *testing.T) {
	song := "test-song.mp3"
	tags, err := ExtractTags("../static/" + song)
	assert.NoError(t, err)
	assert.Equal(t, "Secret Agent Rock", tags["Title"])
	assert.Equal(t, "John Bartmann", tags["Artist"])
	assert.Equal(t, "196", tags["Rating"])
	assert.Contains(t, tags["Album"], "Public Domain")
}

func TestDoNotAddEmpty(t *testing.T) {
	tagMap := make(map[string]string)
	tagMap["ContentType"] = "some/thing"
	addTagIfNotEmpty(tagMap, "e1", " ")
	addTagIfNotEmpty(tagMap, "e2", "")
	addTagIfNotEmpty(tagMap, "c", "content")
	assert.Equal(t, 2, len(tagMap))
}

func TestReadMp3WithPicture(t *testing.T) {
	song := "test-song.mp3"
	tags, err := ExtractTagsAndPicture("../static/"+song, extract)
	assert.NoError(t, err)
	assert.Equal(t, "pic-test.jpg", tags["Picture"])
}

func extract(pic *tag.Picture) (string, error) {
	log.Debug().Msgf("Writing %v", pic)
	name := "pic-test." + pic.Ext
	err := os.WriteFile(name, pic.Data, 0644)
	defer os.Remove(name)
	if err != nil {
		return "", err
	}
	return name, nil
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
