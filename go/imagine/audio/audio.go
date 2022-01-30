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
	return ExtractTagsAndPicture(filename, nil)
}

// ExtractTagsAndPicture extracts tags similar to ExtractTags,
// but also takes a function argument to handle an embedded picture, if any is found
// The returned location from that function is assumed to be the picture's download location,
// and will become the value of the Mao's key "Picture" (e.g. "Picture" => "s3://bucket/covers/rock-it.jpg")
//
// Example content of tag.Picture for sample file:
// "Picture{Ext: jpg, MIMEType: image/jpeg, Type: Cover (front), Description: \u0000, Data.Size: 519650}"
func ExtractTagsAndPicture(filename string, picExtractFunc func(picture *tag.Picture) (string, error)) (map[string]string, error) {
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
	log.Trace().Msgf("Raw Tag: %v", meta.Raw())
	// The detected format + title of the track as per Metadata
	pic := meta.Picture()
	logger.Info().Msgf("%s (%s): %s from %s (hasPic: %v)", filename, meta.Format(), meta.Title(), meta.Artist(), pic != nil)
	if pic != nil && picExtractFunc != nil {
		if location, err := picExtractFunc(pic); err == nil {
			addTagIfNotEmpty(tagMap, "Picture", location)
		} else {
			log.Warn().Msgf("Error extracting pic to %s: %v", location, err)
		}
	}

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

	// Alpha: Rating support. Default rating is 0 so even if no POPM tag is found, we do set the Rating tag
	r := GetRating(meta)
	addTagIfNotEmpty(tagMap, "Rating", r.Numeric())
	return tagMap, nil

}

// addTagIfNotEmpty helper that adds the tag to the map if the value is not empty
func addTagIfNotEmpty(tagMap map[string]string, fieldName string, val string) {
	if len(strings.TrimSpace(val)) > 0 {
		tagMap[fieldName] = val
	}
}
