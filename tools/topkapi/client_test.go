package topkapi

import (
	"fmt"
	"hash/adler32"
	"testing"
)

func TestTopicPrefix(t *testing.T) {
	config := &ClientConfig{
		TopicPrefix: "hase-",
	}
	fulltopic := getTopicWithPrefix("horst",config)
	if fulltopic != "hase-horst" {
		t.Error(fulltopic + " unexpected")
	}
}


func TestChecksum(t *testing.T) {
	message := "SummerOf69!"
	checksum := adler32.Checksum([]byte(message))
	if checksum != 422708159 {
		t.Error(fmt.Sprint(checksum) + " unexpected")
	}
}
