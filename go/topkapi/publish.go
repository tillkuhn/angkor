package topkapi

import (
	"encoding/json"
	"github.com/Shopify/sarama"
	"github.com/hashicorp/go-uuid"
	"time"
)

// PublishEvent expects an Event struct which it will serialize as json before pushing it to the topic
func (c *Client) PublishEvent(event *Event, topic string) (int32, int64, error) {
	event.Time = event.Time.Round(time.Second) // make sure we round to .SSS

	byteMessage, err := json.Marshal(event)
	if err != nil {
		return 0, 0, err
	}
	return c.PublishMessage(byteMessage, topic)
}

// PublishMessage expects a byte message which it will push to the topic
// this is the actual handlers to which other publish functions such as PublishEvent delegate
// See also https://github.com/Shopify/sarama/blob/master/tools/kafka-console-producer/kafka-console-producer.go
func (c *Client) PublishMessage(message []byte, topic string) (int32, int64, error) {
	var partition int32
	var offset int64
	var err error

	messageId, err := uuid.GenerateUUID()
	if err != nil {
		c.logger.Printf("failed to create message id: %v, continue without", err)
	}
	topicWithPrefix := getTopicWithPrefix(topic, c.Config)
	if !c.Config.Enabled {
		c.logger.Printf("Mode 'DISABLED', the following message to topic %s will be suppressed: %s", topicWithPrefix, string(message))
		return 0, 0, nil
	}

	partition, offset, err = c.getSyncProducer().SendMessage(&sarama.ProducerMessage{
		Topic: topicWithPrefix,
		Value: sarama.ByteEncoder(message),
		Headers: []sarama.RecordHeader{
			{ // type sarama.RecordHeader
				Key:   []byte("messageId"),
				Value: []byte(messageId),
			},
			{
				Key:   []byte("schema"),
				Value: []byte("event@" + EventVersion),
			},
			{
				Key:   []byte("clientId"),
				Value: []byte(c.Config.ClientId),
			},
		},
	})
	if err != nil {
		c.logger.Printf("Failed to send message to %s: %v ", topicWithPrefix, err)
		return 0, 0, err
	}

	c.logger.Printf("%v\n", string(message))
	c.logger.Printf("Published event to topic %s/%d/%d with messageId=%s", topicWithPrefix, partition, offset, messageId)
	return partition, offset, nil
}
