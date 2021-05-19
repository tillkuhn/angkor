package topkapi

// Based on https://github.com/Shopify/sarama/tree/master/examples/sasl_scram_client
import (
	"crypto/tls"
	"log"
	"os"
	"strings"

	"github.com/Shopify/sarama"
)


type Producer struct {
	logger *log.Logger
	config *KafkaConfig
	syncProducer sarama.SyncProducer
}

func NewProducer( config *KafkaConfig) *Producer {
	sarama.Logger = log.New(os.Stdout, "[Sarama] ", log.LstdFlags)

	if config.Brokers == "" {
		log.Fatalln("at least one broker is required")
	}
	splitBrokers := strings.Split(config.Brokers, ",")

	if config.SaslUsername == "" {
		log.Fatalln("SASL username is required")
	}

	if config.SaslPassword == "" {
		log.Fatalln("SASL password is required")
	}

	conf := sarama.NewConfig()
	conf.Producer.Retry.Max = 1
	conf.Producer.RequiredAcks = sarama.WaitForAll
	conf.Producer.Return.Successes = true
	conf.Metadata.Full = true
	conf.Version = sarama.V0_10_0_0
	conf.ClientID = "sasl_scram_client"
	conf.Metadata.Full = true
	conf.Net.SASL.Enable = true
	conf.Net.SASL.User = config.SaslUsername
	conf.Net.SASL.Password = config.SaslPassword
	conf.Net.SASL.Handshake = true
	conf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA256} }
	conf.Net.SASL.Mechanism = sarama.SASLTypeSCRAMSHA256
	conf.Net.TLS.Enable = true
	conf.Net.TLS.Config = &tls.Config{
		InsecureSkipVerify: false,
	}
	logger := log.New(os.Stdout, "[Producer] ", log.LstdFlags)
	syncProducer, err := sarama.NewSyncProducer(splitBrokers, conf)
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

func (p *Producer) Publish(message []byte, topic string) {

	topicWithPrefix := p.config.TopicPrefix + topic
	partition, offset, err := p.syncProducer.SendMessage(&sarama.ProducerMessage{
		Topic: topicWithPrefix,
		Value: sarama.ByteEncoder(message),
	})
	if err != nil {
		p.logger.Fatalln("failed to send message to ", topicWithPrefix, err)
	}
	p.logger.Printf("%v\n",string(message))
	p.logger.Printf("wrote message to topic %s at partition: %d, offset: %d", topicWithPrefix, partition, offset)
}


func (p *Producer) Close() {
	p.logger.Println("Closing Sarama Producer")
	if err := p.syncProducer.Close(); err != nil {
		p.logger.Printf("WARNING cannot close producer: %v",err)
	}
}
