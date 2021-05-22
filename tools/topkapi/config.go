package topkapi

import (
	"crypto/tls"
	"fmt"
	"github.com/Shopify/sarama"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
	"log"
	"os"
	"os/user"
	"path/filepath"
)

const kafkaConfigPrefix = "Kafka"

// ClientConfig derived from envConfig
type ClientConfig struct {
	Brokers       string `required:"true" desc:"Comma separated List of brokers" split_words:"true"`
	SaslUsername  string `required:"true" desc:"User for SASL Auth" split_words:"true"`
	SaslPassword  string `required:"true" desc:"Password for SASL Auth" split_words:"true"`
	SaslMechanism string `default:"SCRAM-SHA-256" required:"true" desc:"SASL Mechanism" split_words:"true"`
	TlsEnabled    bool   `default:"true" desc:"TLS Encryption active" split_words:"true"`
	SaslEnabled   bool   `default:"true" desc:"Use SASL Authentication" split_words:"true"`
	TopicPrefix   string `default:"" desc:"Optional prefix, prepended to topic name" split_words:"true"`
	Enabled       bool   `default:"true" desc:"Communication Enabled" split_words:"true"`
	Verbose       bool   `default:"false" desc:"Verbose Logging" split_words:"true"`
	DefaultSource string `default:"" desc:"Default Event Source" split_words:"true"`
	DefaultOffset string `default:"newest" desc:"Default offset, values: newest or oldest" split_words:"true"`
}

func NewConfig() *ClientConfig {
	// Check first if people need helpRequired
	logger := log.New(os.Stdout, fmt.Sprintf("[%-10s] ", "envconfig"), log.LstdFlags)
	var config ClientConfig
	// env file not specified, try user home dir and ~/.angkor
	usr, _ := user.Current()
	for _, dir := range [...]string{".", usr.HomeDir, filepath.Join(usr.HomeDir, ".angkor")} {
		err := godotenv.Load(filepath.Join(dir, ".env"))
		if err == nil {
			logger.Printf("Loading environment vars from %s", filepath.Join(dir, ".env"))
			break
		}
	}

	// Ready for Environment Config, parse Config based on Environment Variables
	err := envconfig.Process(kafkaConfigPrefix, &config)
	if err != nil {
		logger.Fatalf("Error init envconfig: %v", err)
	}

	return &config
}

func initSaramaConfig(config *ClientConfig) *sarama.Config {
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

func getTopicWithPrefix(topic string, config *ClientConfig) string {
	return config.TopicPrefix + topic
}
