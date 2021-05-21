package topkapi

// Based on https://github.com/Shopify/sarama/tree/master/examples/sasl_scram_client
import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"strings"
	"sync"
	"time"

	"github.com/Shopify/sarama"
)

// used to lazy init provider
var mutex = &sync.RWMutex{}

type Event struct {
	Action  string    `json:"action"`
	Message string    `json:"message"`
	Time    time.Time `json:"time"`
	Source  string    `json:"source"`
}

type Client struct {
	// Public
	Config *KafkaConfig

	// Internal
	saramaConfig *sarama.Config
	logger       *log.Logger
	syncProducer sarama.SyncProducer
	brokers      []string
}

// NewClient creates a new client with auto configuration based on envconfig
func NewClient() *Client {
	return NewClientFromConfig(NewConfig())
}

// NewClientFromConfig creates a new client (note that producers will be initialized on demand, so no errors are expected)
func NewClientFromConfig(config *KafkaConfig) *Client {
	// By default it is set to discard all log messages via ioutil.Discard
	sarama.Logger = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", "sarama"), log.LstdFlags)
	client := &Client{
		Config:       config,
		saramaConfig: initSaramaConfig(config),
		logger:       log.New(os.Stdout, fmt.Sprintf("[topkapi💠 ] "), log.LstdFlags),
		syncProducer: nil,
		brokers:      strings.Split(config.Brokers, ","),
	}
	return client
}

// Disable disables all communication (functions can  be called but will only log)
func (c *Client) Disable() {
	c.logger.Println("Client set to mode 'DISABLED'")
	c.Config.Enabled = false
}

// Enable (re)enable communication
func (c *Client) Enable() {
	c.logger.Println("Client set to mode 'ENABLED'")
	c.Config.Enabled = true
}

// PublishEvent expects an Event struct which it will serialize as json before pushing it to the topic
func (c *Client) PublishEvent(event *Event, topic string) (int32, int64, error) {
	byteMessage, err := json.Marshal(event)
	if err != nil {
		return 0, 0, err
	}
	return c.PublishMessage(byteMessage, topic)
}

// PublishMessage expects a byte message, this is the actual handlers to which other publish functions delegate
func (c *Client) PublishMessage(message []byte, topic string) (int32, int64, error) {
	var partition int32
	var offset int64
	var err error

	topicWithPrefix := getTopicWithPrefix(topic, c.Config)
	if ! c.Config.Enabled {
		c.logger.Printf("Mode 'DISABLED', the following message to topic %s will be suppressed: %s", topicWithPrefix, string(message))
		return	0, 0, nil
	}

	partition, offset, err = c.getSyncProducer().SendMessage(&sarama.ProducerMessage{
		Topic: topicWithPrefix,
		Value: sarama.ByteEncoder(message),
	})
	if err != nil {
		c.logger.Println("failed to send message to ", topicWithPrefix, err)
		return 0, 0, err
	}
	c.logger.Printf("%v\n", string(message))
	c.logger.Printf("Published event to topic %s at partition: %d, offset: %d", topicWithPrefix, partition, offset)
	return partition, offset, nil
}

// Close closes the client, if syncProducer is initialized it will also close it
func (c *Client) Close() {
	c.logger.Println("Closing Sarama Client")
	if c.syncProducer != nil {
		if err := c.syncProducer.Close(); err != nil {
			c.logger.Printf("Cannot close producer: %v", err)
		}
	}
}

// See
func (c *Client) getSyncProducer() sarama.SyncProducer {
	// https://launchdarkly.com/blog/golang-pearl-thread-safe-writes-and-double-checked-locking-in-go/
	mutex.RLock()
	if c.syncProducer == nil {
		mutex.RUnlock()
		mutex.Lock()
		defer mutex.Unlock()
		if c.syncProducer == nil {
			// time.Sleep(5 * time.Second)
			c.logger.Println("First call to publish, init syncProducer")
			var err error
			c.syncProducer, err = sarama.NewSyncProducer(c.brokers, c.saramaConfig)
			if err != nil {
				c.logger.Fatalln("Failed to create producer: ", err)
			}

		}
		return c.syncProducer
	} else {
		defer mutex.RUnlock()
		return c.syncProducer
	}
}
