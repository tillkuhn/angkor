package main

import (
	"encoding/json"
	"log"
	"strings"
	"sync"
	"time"

	"github.com/Shopify/sarama"
	"github.com/tillkuhn/angkor/go/topkapi"
)

func consumeEvents(client *topkapi.Client, actions map[string]int) {
	topic := "audit,system,app"
	// so we can lock while writing to the map since multiple message consumers may execute concurrently
	mu := &sync.Mutex{}
	client.Config.OffsetMode = "oldest" // default is 'newest'
	client.Config.ConsumerTimeout = 10 * time.Second
	topicsSlice := strings.Split(topic, ",")
	var messageHandler topkapi.MessageHandler = func(message *sarama.ConsumerMessage) {
		// Parse JSON body into an event
		var event topkapi.Event // var notes []interface{} is now a concrete struct
		err := json.Unmarshal(message.Value, &event)
		if err != nil {
			logger.Printf("Error: Cannot convert messageVal %s into json event: %v", string(message.Value), err)
			return
		}
		// https://stackoverflow.com/questions/36167200/how-safe-are-golang-maps-for-concurrent-read-write-operations
		mu.Lock()
		if val, ok := actions[event.Action]; ok {
			actions[event.Action] = val + 1
		} else {
			actions[event.Action] = 1
		}
		mu.Unlock()
		log.Printf("Consumed Message: action = %s, message=%s, timestamp = %v, topic = %s", event.Action, event.Message, message.Timestamp, message.Topic)
	}
	if err := client.Consume(messageHandler, topicsSlice...); err != nil {
		logger.Fatalf("Error Consuming from %s: %v", topic, err)
	}
}
