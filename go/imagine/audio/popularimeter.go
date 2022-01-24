package audio

import (
	"bytes"
	"encoding/binary"
	"fmt"

	"github.com/dhowden/tag"
	"github.com/rs/zerolog/log"
)

// Rating provides experimental access to MP3 Popularimeter aka Star rating
// See https://stackoverflow.com/questions/52914085/how-to-get-and-print-mp3-popularimeter-frame-with-golang-id3-go-library
type Rating uint8

func (r Rating) String() string {
	if r == 0 {
		return "unknown"
	}
	return fmt.Sprintf("%d/255", r)
}

func (r Rating) Numeric() string {
	return fmt.Sprintf("%d", r)
}

type Popularimeter struct {
	Email   string
	Rating  Rating
	Counter uint32
}

func GetRating(meta tag.Metadata) Rating {
	var rating = Rating(0)
	key, exists := meta.Raw()["POPM"]
	if !exists {
		log.Warn().Msgf("cannot locate POPM tag for title %s", meta.Title())
		return rating
	}
	buf, ok := key.([]byte)
	if !ok {
		log.Warn().Msgf("cannot cat POPM tag to []byte for title %s: %v ", meta.Title(), key)
		return rating
	}
	popularimeter, err := extractPopularimeter(buf)
	if err == nil {
		rating = popularimeter.Rating
	} else {
		log.Warn().Msgf("cannot extract rating for %s: %v", meta.Title(), err)
	}
	return rating
}

func extractPopularimeter(data []byte) (*Popularimeter, error) {

	tokens := bytes.SplitN(data, []byte{0x0}, 2)
	// Popularimeter: <string>, null, 1byte rating, 4bytes counter
	if len(tokens) != 2 {
		return nil, fmt.Errorf("invalid Popularimeter. %d tokens found, expected 2", len(tokens))
	}
	if len(tokens[1]) == 5 {
		return &Popularimeter{
			Email:   string(tokens[0]),
			Rating:  Rating(tokens[1][0]),
			Counter: binary.BigEndian.Uint32(tokens[1][1:]),
		}, nil
	} else /* no counter */ {
		return &Popularimeter{
			Email:   string(tokens[0]),
			Rating:  Rating(tokens[1][0]),
			Counter: 0,
		}, nil
	}
}
