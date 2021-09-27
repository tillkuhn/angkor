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
	fulltopic := getTopicWithPrefix("horst", config)
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

func TestClientSetup(t *testing.T) {
	expected := "testTheRest"
	client := NewClientWithId(expected)
	actual1 := client.Config.ClientId
	if actual1 != expected {
		t.Error(fmt.Sprintf("Expected %s got %s", expected, actual1))
	}
}
