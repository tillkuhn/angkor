// Package audio provides function to analyze tags of mp3 media files
// Kudos to: https://github.com/dhowden/tag
package audio

import (
	"os"
	"strconv"
	"strings"

	"github.com/dhowden/tag"
	"github.com/rs/zerolog/log"
)

// ExtractTags extracts EXIF data from mp3 and puts it into a simple map
// suitable for S3 Object Tags
func ExtractTags(filename string) (map[string]string, error) {
	logger := log.Logger.With().Str("logger", "audio").Logger()
	tagMap := make(map[string]string)
	songFile, err := os.Open(filename)
	if err != nil {
		return tagMap, err
	}
	meta, err := tag.ReadFrom(songFile)
	if err != nil {
		return tagMap, err
	}
	log.Trace().Msgf("%v", meta.Raw())
	// The detected format + title of the track as per Metadata
	logger.Info().Msgf("%s (%s): %s from %s", filename, meta.Format(), meta.Title(), meta.Artist())

	addTagIfNotEmpty(tagMap, "Title", meta.Title())
	addTagIfNotEmpty(tagMap, "Artist", meta.Artist())
	addTagIfNotEmpty(tagMap, "Genre", meta.Genre())
	addTagIfNotEmpty(tagMap, "Album", meta.Album())
	addTagIfNotEmpty(tagMap, "Year", strconv.Itoa(meta.Year()))

	track, total := meta.Track()
	var trackStr strings.Builder
	if track > 0 {
		trackStr.WriteString(strconv.Itoa(track))
	}
	if total > 0 {
		trackStr.WriteString("/" + strconv.Itoa(total))
	}
	addTagIfNotEmpty(tagMap, "Track", trackStr.String())

	return tagMap, nil

}

// addTagIfNotEmpty helper that adds the tag to the map if the value is not empty
func addTagIfNotEmpty(tagMap map[string]string, fieldName string, val string) {
	if len(strings.TrimSpace(val)) > 0 {
		tagMap[fieldName] = val
	}
}
