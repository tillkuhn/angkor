package topkapi

import (
	"github.com/Shopify/sarama"
	"log"
	"os"
	"os/signal"
	"strings"
)

type Consumer struct {
	logger *log.Logger
	config *KafkaConfig
}

func NewConsumer( config *KafkaConfig) *Consumer {
	c := &Consumer{
		logger: log.New(os.Stdout, "[Consumer] ", log.LstdFlags),
		config: config,
	}
	return c
}

func (c *Consumer) Consume(topic string) {
	saramaConfig := saramaConfig(c.config)
	splitBrokers :=  strings.Split(c.config.Brokers, ",")
	consumer, err := sarama.NewConsumer(splitBrokers, saramaConfig)
	if err != nil {
		panic(err)
	}
	log.Println("consumer created")
	defer func() {
		if err := consumer.Close(); err != nil {
			log.Fatalln(err)
		}
	}()
	topicWithPrefix := getTopicWithPrefix(topic, c.config)
	log.Printf("commence consuming topic %s",topicWithPrefix)
	partitionConsumer, err := consumer.ConsumePartition(topicWithPrefix, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := partitionConsumer.Close(); err != nil {
			log.Printf("Error closing partitionConsumer: %v", err)
		}
	}()

	// Trap SIGINT to trigger a shutdown.
	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt)

	consumed := 0
ConsumerLoop:
	for {
		log.Println("in the for")
		select {
		case msg := <-partitionConsumer.Messages():
			log.Printf("Consumed message offset %d\n", msg.Offset)
			log.Printf("KEY: %s VALUE: %s", msg.Key, msg.Value)
			consumed++
		case <-signals:
			break ConsumerLoop
		}
	}

	log.Printf("Consumed: %d\n", consumed)

}
