package topkapi

import (
	"context"
	"github.com/Shopify/sarama"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"
)

// Consume is a blocking function that reads message from a topic
// todo add consumeOnce function or flag to prevent infinite loop
func (c *Client) Consume(messageHandler MessageHandler, topicsWithoutPrefix ...string) error {
	group := c.Config.ClientId
	// topics ...string = variadic function which results in []string slice
	var topics []string
	for _, t := range topicsWithoutPrefix {
		topics = append(topics, getTopicWithPrefix(t, c.Config))
	}
	offset := sarama.OffsetNewest
	if strings.ToLower(c.Config.OffsetMode) == "oldest" {
		offset = sarama.OffsetOldest
	}
	c.logger.Printf("Creating consumerGroup=%s to consume topics=%v kafkaVersion=%s", group, topics, c.saramaConfig.Version)

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
	// https://www.sohamkamani.com/golang/2018-06-17-golang-using-context-cancellation/
	// https://stackoverflow.com/questions/47417597/conditional-cases-in-go-select-statement
	ctx, cancel := context.WithCancel(context.Background())
	client, err := sarama.NewConsumerGroup(c.brokers, group, c.saramaConfig)
	if err != nil {
		c.logger.Panicf("Error creating consumer group client: %v", err)
	}

	waitGroup := &sync.WaitGroup{} // // A WaitGroup waits for a collection of goroutines to finish.
	waitGroup.Add(1)               // Add adds delta, which may be negative, to the WaitGroup counter.
	go func() {
		defer waitGroup.Done()
		for {
			// `Consume` should be called inside an infinite loop, when a
			// server-side rebalance happens, the consumer session will need to be
			// recreated to get the new claims

			// Consume joins a cluster of consumers for a given list of topics and
			// starts a blocking ConsumerGroupSession through the ConsumerGroupHandler.
			// 4. The session will persist until one of the ConsumeClaim() functions exits. This can be either when the
			//    parent context is cancelled or when a server-side rebalance cycle is initiated.
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
	c.logger.Printf("SaramaConsumer consumer is ready groupId=%s offsetMode=%s", group, c.Config.OffsetMode)

	sigtermChannel := make(chan os.Signal, 1)
	signal.Notify(sigtermChannel, syscall.SIGINT, syscall.SIGTERM)
	// This blocks until cancelled or terminated
	if c.Config.ConsumerTimeout == 0 {
		c.logger.Print("Consuming forever until cancel or Signal is caught")
		select {
		case <-ctx.Done(): // a Done channel for cancellation.
			c.logger.Println("terminating: context cancelled")
		case <-sigtermChannel: // channel to receive os.Signals
			c.logger.Println("terminating: via signal")
		}
	} else {
		c.logger.Printf("Timeout requested, consuming for %v", c.Config.ConsumerTimeout)
		select {
		case <-time.After(c.Config.ConsumerTimeout): /* 5 * time.Second*/
			c.logger.Printf("Consuming finished after %v", c.Config.ConsumerTimeout)
		case <-sigtermChannel: // channel to receive os.Signals
			c.logger.Println("terminating: via signal")
		}
	}
	cancel()         // A CancelFunc tells an operation to abandon its work.
	waitGroup.Wait() // Wait blocks until the WaitGroup counter is zero.
	// Close stops the ConsumerGroup and detaches any running sessions.
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

// MessageHandler is our custom handler attached to the SaramaConsumer
// supposed to be passed in by the caller
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
	// NOTE: Do not move the code below to a goroutine.
	// The `ConsumeClaim` itself is called within a goroutine, see:
	// https://github.com/Shopify/sarama/blob/master/consumer_group.go#L27-L29
	for message := range claim.Messages() {
		// log.Printf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		consumer.messageHandler(message)
		session.MarkMessage(message, "")
	}
	return nil
}
