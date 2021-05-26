package topkapi

// Based on https://github.com/Shopify/sarama/tree/master/examples/sasl_scram_client
import (
	"encoding/json"
	"fmt"
	"github.com/kelseyhightower/envconfig"
	"io/ioutil"
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
	Action  string    `json:"action,omitempty"`
	Message string    `json:"message,omitempty"`
	Time    time.Time `json:"time,omitempty"`
	Source  string    `json:"source,omitempty"`
	EntityId string    `json:"entityId,omitempty"`
}

type Client struct {
	// Public
	Config *ClientConfig

	// Internal
	saramaConfig *sarama.Config
	logger       *log.Logger
	syncProducer sarama.SyncProducer
	brokers      []string
}

// NewClient creates a new client with auto configuration based on envconfig
// and argv[0] as default clientId
func NewClient() *Client {
	return NewClientFromConfig(NewConfig())
}

// NewClientWithId creates a new client the given clientId and default config
func NewClientWithId(clientId string) *Client {
	c := NewConfig()
	c.ClientId = clientId
	return NewClientFromConfig(c)
}

// NewClientFromConfig creates a new client (note that producers will be initialized on demand, so no errors are expected)
func NewClientFromConfig(config *ClientConfig) *Client {
	// By default it is set to discard all log messages via ioutil.Discard
	if config.ClientId == "" {
		config.ClientId = os.Args[0]
	}
	client := &Client{
		Config:       config,
		saramaConfig: initSaramaConfig(config),
		logger:       log.New(os.Stdout, fmt.Sprintf("[topkapi💠 ] "), log.LstdFlags),
		syncProducer: nil,
		brokers:      strings.Split(config.Brokers, ","),
	}
	configureSaramaLogger(config.Verbose)
	return client
}

// Verbose configure verbose logging for sarama functions
// Default value is false
func (c *Client) Verbose(enabled bool) {
	c.logger.Printf("Client set verbose mode enabled=%v",enabled)
	c.Config.Verbose = enabled
	configureSaramaLogger(enabled)
}

// Enable disables all communication (functions can  be called but will only log)
// Default value is true
func (c *Client) Enable(enabled bool) {
	c.logger.Printf("Client set to mode enabled=%v",enabled)
	c.Config.Enabled = enabled
}

// PublishEvent expects an Event struct which it will serialize as json before pushing it to the topic
func (c *Client) PublishEvent(event *Event, topic string) (int32, int64, error) {
	byteMessage, err := json.Marshal(event)
	if err != nil {
		return 0, 0, err
	}
	return c.PublishMessage(byteMessage, topic)
}

// Usage prints usage to STDOUT, deleagting to envconfig
func (c *Client) Usage() {
	envconfig.Usage(kafkaConfigPrefix, c.Config)
}

// PublishMessage expects a byte message, this is the actual handlers to which other publish functions delegate
// See also https://github.com/Shopify/sarama/blob/master/tools/kafka-console-producer/kafka-console-producer.go
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
	c.logger.Println("Closing SaramaConsumer Client")
	if c.syncProducer != nil {
		if err := c.syncProducer.Close(); err != nil {
			c.logger.Printf("Cannot close producer: %v", err)
		}
	}
}

// NewEvent inits a new event with reasonable defaults
func  (c *Client) NewEvent(action string, message string) *Event {
	return &Event{
		Time:    time.Now(),
		Action:  action,
		Message: message,
		Source:  c.Config.ClientId,
	}
}

// returns singleton sync producer instance
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

func configureSaramaLogger(enabled bool) {
	var logTarget = ioutil.Discard
	if enabled {
		logTarget = os.Stdout
	}
	sarama.Logger = log.New(logTarget, fmt.Sprintf("[%-10s] ", "sarama"), log.LstdFlags)

}
