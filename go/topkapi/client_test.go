package topkapi

import (
	"fmt"
	"hash/adler32"
	"os"
	"testing"
)

func TestTopicPrefix(t *testing.T) {
	config := &ClientConfig{
		TopicPrefix: "hase-",
	}
	fullTopic := getTopicWithPrefix("horst", config)
	if fullTopic != "hase-horst" {
		t.Error(fullTopic + " unexpected")
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
	_ = os.Setenv("KAFKA_BROKERS", "localhost:9094")
	_ = os.Setenv("KAFKA_SASL_USERNAME", "hasentiger123") // KAFKA_SASL_USERNAME is mandatory
	_ = os.Setenv("KAFKA_SASL_PASSWORD", "test")          // also mandatory
	expected := "testTheRest"
	client := NewClientWithId(expected)
	actual1 := client.Config.ClientId
	if actual1 != expected {
		t.Error(fmt.Sprintf("Expected %s got %s", expected, actual1))
	}
}
