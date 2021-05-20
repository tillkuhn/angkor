package topkapi

import (
	"crypto/tls"
	"github.com/Shopify/sarama"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
	"log"
	"os/user"
	"path/filepath"
)

const kafkaConfigPrefix = "Kafka"

// KafkaConfig derived from envConfig
type KafkaConfig struct {
	Brokers       string `required:"true" desc:"Comma separated List of brokers" split_words:"true"`
	SaslUsername  string `required:"true" desc:"User for SASL Auth" split_words:"true"`
	SaslPassword  string `required:"true" desc:"Password for SASL Auth" split_words:"true"`
	SaslMechanism string `default:"SCRAM-SHA-256" required:"true" desc:"SASL Mechanism" split_words:"true"`
	TlsEnabled    bool   `default:"true" desc:"TLS Encryption active" split_words:"true"`
	SaslEnabled   bool   `default:"true" desc:"Use SASL Authentication" split_words:"true"`
	TopicPrefix   string   `default:"" desc:"Optional prefix, prepended to topic name" split_words:"true"`
}

func NewConfig() *KafkaConfig {
	// Check first if people need helpRequired
	var config KafkaConfig
	// env file not specified, try user home dir and ~/.angkor
	usr, _ := user.Current()
	for _, dir := range [...]string{".", usr.HomeDir, filepath.Join(usr.HomeDir, ".angkor")} {
		err := godotenv.Load(filepath.Join(dir, ".env"))
		if err == nil {
			log.Printf("Loading environment vars from %s", filepath.Join(dir, ".env"))
			break
		}
	}

	// Ready for Environment config, parse config based on Environment Variables
	err := envconfig.Process(kafkaConfigPrefix, &config)
	if err != nil {
		log.Fatalf("Error init envconfig: %v", err)
	}

	return &config
}

func saramaConfig(config *KafkaConfig) *sarama.Config {
	if config.Brokers == "" {
		log.Fatalln("at least one broker is required")
	}

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
	return conf
}

func getTopicWithPrefix(topic string, config *KafkaConfig) string {
	return config.TopicPrefix + topic
}
