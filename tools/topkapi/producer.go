package topkapi

// Based on https://github.com/Shopify/sarama/tree/master/examples/sasl_scram_client
import (
	"encoding/json"
	"log"
	"os"
	"strings"
	"time"

	"github.com/Shopify/sarama"
)


type Event struct {
	Action  string `json:"action"`
	Message string `json:"message"`
	Time time.Time `json:"time"`
	Source string `json:"source"`
}

type Producer struct {
	logger *log.Logger
	config *KafkaConfig
	syncProducer sarama.SyncProducer
}

func NewProducer( config *KafkaConfig) *Producer {
	sarama.Logger = log.New(os.Stdout, "[Sarama] ", log.LstdFlags)
	saramaConfig := saramaConfig(config)
	logger := log.New(os.Stdout, "[Producer] ", log.LstdFlags)
	splitBrokers :=  strings.Split(config.Brokers, ",")
	syncProducer, err := sarama.NewSyncProducer(splitBrokers, saramaConfig)
	if err != nil {
		logger.Fatalln("failed to create producer: ", err)
	}
	p := &Producer{
		logger: logger,
		syncProducer: syncProducer,
		config: config,
	}
	return p
}

func (p *Producer) PublishEvent(event *Event, topic string) error {
	byteMessage, err := json.Marshal(event)
	if err != nil {
		return err
	}
	return p.PublishMessage(byteMessage, topic)
}

func (p *Producer) PublishMessage(message []byte, topic string) error {

	topicWithPrefix := getTopicWithPrefix(topic, p.config)
	partition, offset, err := p.syncProducer.SendMessage(&sarama.ProducerMessage{
		Topic: topicWithPrefix,
		Value: sarama.ByteEncoder(message),
	})
	if err != nil {
		p.logger.Println("failed to send message to ", topicWithPrefix, err)
		return err
	}
	p.logger.Printf("%v\n",string(message))
	p.logger.Printf("wrote message to topic %s at partition: %d, offset: %d", topicWithPrefix, partition, offset)
	return nil
}


func (p *Producer) Close() {
	p.logger.Println("Closing Sarama Producer")
	if err := p.syncProducer.Close(); err != nil {
		p.logger.Printf("WARNING cannot close producer: %v",err)
	}
}

