package topkapi

import (
	"context"
	"github.com/Shopify/sarama"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
)

// Consume is a blocking function that reads message from a topic
// todo add consumeOnce function or flag to prevent infinite loop
func (c *Client) Consume(messageHandler MessageHandler, topicsWithoutPrefix ...string) error {
	group := c.Config.ClientId
	// topics ...string = variadic function which results in []string slice
	var topics []string
	for _, t := range topicsWithoutPrefix {
		topics = append(topics, getTopicWithPrefix(t,c.Config))
	}
	offset := sarama.OffsetNewest
	if strings.ToLower(c.Config.OffsetMode) == "oldest" {
		offset = sarama.OffsetOldest
	}
	// The Kafka cluster version has to be defined before the consumer/producer is initialized.
	// consumer groups require Version to be >= V0_10_2_0) see https://www.cloudkarafka.com/changelog.html
	kafkaVersion,err := sarama.ParseKafkaVersion(c.Config.KafkaVersion)
	if err != nil {
		return err
	}
	c.logger.Printf("Creating consumerGroup=%s to consume topics=%v kafkaVersion=%s", group, topics, kafkaVersion)
	c.saramaConfig.Version = kafkaVersion

	// consumer, err := sarama.NewConsumer(c.brokers, c.saramaConfig)
	// https://github.com/Shopify/sarama/blob/master/examples/consumergroup/main.go
	// BalanceStrategySticky, BalanceStrategyRoundRobin or BalanceStrategyRange
	c.saramaConfig.Consumer.Group.Rebalance.Strategy = sarama.BalanceStrategyRoundRobin

	// Should be OffsetNewest or OffsetOldest. Defaults to OffsetNewest.
	c.saramaConfig.Consumer.Offsets.Initial = offset

	 // Setup a new SaramaConsumer consumer group
	consumer := SaramaConsumer{
		ready:          make(chan bool),
		messageHandler: messageHandler,
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
	c.logger.Printf("SaramaConsumer consumer up and running groupId=%s offsetMode=%s", group, c.Config.OffsetMode)

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
		c.logger.Printf("warning - Error closing client: %v", err)
	}
	return nil
}

// SaramaConsumer represents a SaramaConsumer consumer group consumer
type SaramaConsumer struct {
	ready          chan bool
	messageHandler MessageHandler
}
type MessageHandler func(message *sarama.ConsumerMessage)

// Setup is run at the beginning of a new session, before ConsumeClaim
func (consumer *SaramaConsumer) Setup(sarama.ConsumerGroupSession) error {
	// Mark the consumer as ready
	close(consumer.ready)
	return nil
}

// Cleanup is run at the end of a session, once all ConsumeClaim goroutines have exited
func (consumer *SaramaConsumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim must start a consumer loop of ConsumerGroupClaim's Messages().
func (consumer *SaramaConsumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	// NOTE:
	// Do not move the code below to a goroutine.
	// The `ConsumeClaim` itself is called within a goroutine, see:
	// https://github.com/Shopify/sarama/blob/master/consumer_group.go#L27-L29
	for message := range claim.Messages() {
		// log.Printf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		consumer.messageHandler(message)
		session.MarkMessage(message, "")
	}
	return nil
}
