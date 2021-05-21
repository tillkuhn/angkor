package topkapi

import "testing"

func TestTopicPrefix(t *testing.T) {
	config := &ClientConfig{
		TopicPrefix: "hase-",
	}
	fulltopic := getTopicWithPrefix("horst",config)
	if fulltopic != "hase-horst" {
		t.Error(fulltopic + " unexpected")
	}
}
