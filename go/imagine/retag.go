package main

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/rs/zerolog/log"
	"github.com/tillkuhn/angkor/tools/imagine/audio"
)

// Retag checks all existing mp3 files of missing key tags (Artist, Title ...) and triggers a scan if incomplete
func Retag() {
	logger := log.Logger.With().Str("logger", "retag").Logger()

	prefix := fmt.Sprintf("%s%s/", config.S3Prefix, "songs")
	logger.Debug().Msgf("Checking path %s for songs that are not fully tagged", prefix)
	resp, _ := s3Handler.ListObjectsForEntity(prefix)
	updCnt := 0
	for _, song := range resp.Items {
		if filepath.Ext(song.Path) != ".mp3" {
			continue
		}
		if _, hasArtist := song.Tags["Artist"]; !hasArtist {
			log.Debug().Msgf("Path %v has no artist", song.Path)
			tmpFile, err := s3Handler.DownloadObject(song.Path)
			if err != nil {
				log.Err(err).Msgf("Cannot download to %s: %s", tmpFile, err.Error())
				continue
			}
			updTags, _ := audio.ExtractTags(tmpFile)
			// even in case of error, we don't need the temporary file anymore
			if err = os.Remove(tmpFile); err != nil {
				log.Warn().Msgf("WARN: Could not delete temp file %s: %v", tmpFile, err)
			}

			if err := s3Handler.PutTags(song.Path, updTags); err != nil {
				log.Err(err).Msgf("Cannot update tags for %s: %s", tmpFile, err.Error())
			}
			updCnt++
		}
	}
	log.Info().Msgf("Pulled %d objects, %d update due to missing tags", len(resp.Items), updCnt)

}
