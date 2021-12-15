package audio

import (
	"github.com/dhowden/tag"
	"github.com/rs/zerolog/log"
	"os"
)

// https://github.com/dhowden/tag

// ExtractTags extracts EXIF data from mp3 and puts it into a simple map
// suitable for S3 Object Tags
func ExtractTags(filename string) (map[string]string, error) {
	tagMap := make(map[string]string)
	logger := log.Logger.With().Str("logger", "audio").Logger()
	songFile, err := os.Open(filename)
	if err != nil {
		return tagMap, err
	}
	meta, err := tag.ReadFrom(songFile)
	if err != nil {
		return tagMap, err
	}
	logger.Info().Msgf("%s (%s): %s from %s", filename, meta.Format(), meta.Title(), meta.Artist()) // The detected format + title of the track as per Metadata
	addTag(meta.Title(), tagMap, "Title")
	addTag(meta.Artist(), tagMap, "Artist")
	addTag(meta.Genre(), tagMap, "Genre")
	return tagMap, nil

}

// addTag adds the tag to the map if the value is not empty
func addTag(val string, tagMap map[string]string, fieldName string) {
	if len(val) > 0 {
		tagMap[fieldName] = val
	}
}
