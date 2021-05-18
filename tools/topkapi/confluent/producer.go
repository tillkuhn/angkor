package confluent

import (
	"github.com/tillkuhn/angkor/tools/topkapi/pkg"
	"log"
	"os"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func Publish(message []byte) {
	// Let me introduce myself - I'm Remindabot
	// configure delegates most of the work to envconfig
	config := pkg.configure()

	kafkaConfig := &kafka.ConfigMap{
		"metadata.broker.list": config.Brokers,
		"sasl.username":        config.SaslUsername,
		"sasl.password":        config.SaslPassword,
		"security.protocol":    "SASL_SSL",
		"sasl.mechanisms":      config.SaslMechanism,
		// "group.id":            "", //os.Getenv("CLOUDKARAFKA_GROUPID"), // consumer property !!!
		// "default.topic.kafkaConfig": kafka.ConfigMap{"auto.offset.reset": "earliest"}, // consumer property !!!
		//"debug":                           "generic,broker,security",
	}
	// os.Getenv("CLOUDKARAFKA_TOPIC_PREFIX"
	topicName := config.SaslUsername + "-" + "system"
	producer, err := kafka.NewProducer(kafkaConfig)
	if err != nil {
		log.Printf("Failed to create producer: %v\n", err)
		os.Exit(1)
	}
	log.Printf("Created Producer %v\n", producer)
	// createTopic(topicName, producer) // always gives error
	deliveryChan := make(chan kafka.Event)

	err = producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topicName, Partition: kafka.PartitionAny}, Value: message}, deliveryChan)
	e := <-deliveryChan
	m := e.(*kafka.Message)
	if m.TopicPartition.Error != nil {
		log.Printf("Delivery failed: %v\n", m.TopicPartition.Error)
	} else {
		log.Printf("Delivered message to topic %s [%d] at offset %v\n",
			*m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset)
	}
	close(deliveryChan)
}

//// https://github.com/confluentinc/confluent-kafka-go/blob/master/examples/confluent_cloud_example/confluent_cloud_example.go
//func createTopic(topic string, producer *kafka.Producer) {
//
//	fmt.Printf("Creating topic %s\n",topic)
//	adminClient, err := kafka.NewAdminClientFromProducer(producer)
//
//	if err != nil {
//		fmt.Printf("Failed to create Admin client: %s\n", err)
//		os.Exit(1)
//	}
//	// Contexts are used to abort or limit the amount of time
//	// the Admin call blocks waiting for a result.
//	ctx, cancel := context.WithCancel(context.Background())
//	defer cancel()
//
//	// Create topics on cluster.
//	// Set Admin options to wait for the operation to finish (or at most 60s)
//	maxDuration, err := time.ParseDuration("60s")
//	if err != nil {
//		panic("time.ParseDuration(60s)")
//	}
//
//	results, err := adminClient.CreateTopics(ctx,
//		[]kafka.TopicSpecification{{
//			Topic:             topic,
//			NumPartitions:     1,
//			ReplicationFactor: 1}},
//		kafka.SetAdminOperationTimeout(maxDuration))
//
//	if err != nil {
//		fmt.Printf("Problem during the topic creation: %v\n", err)
//		os.Exit(1)
//	}
//
//	// Check for specific topic errors
//	for _, result := range results {
//		if result.Error.Code() != kafka.ErrNoError &&
//			result.Error.Code() != kafka.ErrTopicAlreadyExists {
//			fmt.Printf("Topic creation failed for %s: %v Fullresult %v", result.Topic, result.Error.String())
//			os.Exit(1)
//		}
//	}
//
//	adminClient.Close()
//
//}
