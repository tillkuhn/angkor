package topkapi

import (
	"context"
	"github.com/Shopify/sarama"
	"log"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
)

// Consume is a blocking function that reads message from a topic
func (c *Client) Consume(topic string) {
	group := c.Config.DefaultSource
	topics := []string{getTopicWithPrefix(topic, c.Config)}
	offset := sarama.OffsetNewest
	if strings.ToLower(c.Config.DefaultOffset) == "oldest" {
		offset = sarama.OffsetOldest
	}
	// The Kafka cluster version has to be defined before the consumer/producer is initialized.
	// consumer groups require Version to be >= V0_10_2_0) see https://www.cloudkarafka.com/changelog.html
	kafkaVersion := sarama.V2_6_0_0
	c.logger.Printf("Creating consumerGroup=%s to consume topics=%v kafkaVersion=%s", group, topics, kafkaVersion)
	c.saramaConfig.Version = kafkaVersion

	// consumer, err := sarama.NewConsumer(c.brokers, c.saramaConfig)
	// https://github.com/Shopify/sarama/blob/master/examples/consumergroup/main.go
	// BalanceStrategySticky, BalanceStrategyRoundRobin or BalanceStrategyRange
	c.saramaConfig.Consumer.Group.Rebalance.Strategy = sarama.BalanceStrategyRoundRobin
	// Should be OffsetNewest or OffsetOldest. Defaults to OffsetNewest.
	c.saramaConfig.Consumer.Offsets.Initial = offset
	/**
	 * Setup a new Sarama consumer group
	 */
	consumer := Consumer{
		ready: make(chan bool),
	}
	ctx, cancel := context.WithCancel(context.Background())
	client, err := sarama.NewConsumerGroup(c.brokers, group, c.saramaConfig)
	if err != nil {
		c.logger.Panicf("Error creating consumer group client: %v", err)
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			// `Consume` should be called inside an infinite loop, when a
			// server-side rebalance happens, the consumer session will need to be
			// recreated to get the new claims
			if err := client.Consume(ctx, topics, &consumer); err != nil {
				c.logger.Panicf("Error from consumer: %v", err)
			}
			// check if context was cancelled, signaling that the consumer should stop
			if ctx.Err() != nil {
				return
			}
			consumer.ready = make(chan bool)
		}
	}()

	<-consumer.ready // Await till the consumer has been set up
	c.logger.Println("Sarama consumer up and running!...")

	sigterm := make(chan os.Signal, 1)
	signal.Notify(sigterm, syscall.SIGINT, syscall.SIGTERM)
	select {
	case <-ctx.Done():
		c.logger.Println("terminating: context cancelled")
	case <-sigterm:
		c.logger.Println("terminating: via signal")
	}
	cancel()
	wg.Wait()
	if err = client.Close(); err != nil {
		c.logger.Printf("Error closing client: %v", err)
	}
}

// Consumer represents a Sarama consumer group consumer
type Consumer struct {
	ready chan bool
}

// Setup is run at the beginning of a new session, before ConsumeClaim
func (consumer *Consumer) Setup(sarama.ConsumerGroupSession) error {
	// Mark the consumer as ready
	close(consumer.ready)
	return nil
}

// Cleanup is run at the end of a session, once all ConsumeClaim goroutines have exited
func (consumer *Consumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim must start a consumer loop of ConsumerGroupClaim's Messages().
func (consumer *Consumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	// NOTE:
	// Do not move the code below to a goroutine.
	// The `ConsumeClaim` itself is called within a goroutine, see:
	// https://github.com/Shopify/sarama/blob/master/consumer_group.go#L27-L29
	for message := range claim.Messages() {
		log.Printf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		session.MarkMessage(message, "")
	}

	return nil
}
