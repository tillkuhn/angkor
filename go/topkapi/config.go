package topkapi

import (
	"crypto/tls"
	"os/user"
	"path/filepath"
	"time"

	"github.com/Shopify/sarama"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
	"github.com/rs/zerolog/log"
)

const kafkaConfigPrefix = "Kafka"

// ClientConfig derived from envConfig
type ClientConfig struct {
	Brokers         string        `required:"true" desc:"Comma separated List of brokers" split_words:"true"`
	SaslUsername    string        `required:"true" desc:"User for SASL Auth" split_words:"true"`
	SaslPassword    string        `required:"true" desc:"Password for SASL Auth" split_words:"true"`
	SaslMechanism   string        `default:"SCRAM-SHA-256" required:"true" desc:"SASL Mechanism" split_words:"true"`
	TlsEnabled      bool          `default:"true" desc:"TLS Encryption active" split_words:"true"`
	SaslEnabled     bool          `default:"true" desc:"Use SASL Authentication" split_words:"true"`
	TopicPrefix     string        `default:"" desc:"Optional prefix, prepended to topic name and empty by default" split_words:"true"`
	Enabled         bool          `default:"true" desc:"Communication Enabled" split_words:"true"`
	Verbose         bool          `default:"false" desc:"Verbose Logging" split_words:"true"`
	ClientId        string        `default:"" desc:"ClientId, will be also used as default source" split_words:"true"`
	OffsetMode      string        `default:"newest" desc:"Default offset for consumer, values: newest or oldest" split_words:"true"`
	KafkaVersion    string        `default:"2.6.0" desc:"Version of Kafka, important for initiating consumer group"`
	ConsumerTimeout time.Duration `desc:"Duration how long the consumer is looping (default: forever)"`
}

func NewConfig() *ClientConfig {
	// Check first if people need helpRequired
	logger := log.Logger.With().Str("logger", "⚙️ envconfig").Logger()
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
		logger.Fatal().Msgf("Error init envconfig: %v", err)
	}

	return &config
}

func initSaramaConfig(config *ClientConfig) *sarama.Config {
	if config.Brokers == "" {
		log.Fatal().Msg("at least one broker is required")
	}

	if config.SaslUsername == "" {
		log.Fatal().Msg("SASL username is required")
	}

	if config.SaslPassword == "" {
		log.Fatal().Msg("SASL password is required")
	}

	saramaConf := sarama.NewConfig()
	saramaConf.Producer.Retry.Max = 1
	saramaConf.Producer.RequiredAcks = sarama.WaitForAll
	saramaConf.Producer.Return.Successes = true
	saramaConf.Metadata.Full = true
	saramaConf.Version = sarama.V0_10_0_0 // KafkaVersion
	saramaConf.ClientID = config.ClientId
	saramaConf.Metadata.Full = true
	saramaConf.Net.SASL.Enable = true
	saramaConf.Net.SASL.User = config.SaslUsername
	saramaConf.Net.SASL.Password = config.SaslPassword
	saramaConf.Net.SASL.Handshake = true
	saramaConf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA256} }
	saramaConf.Net.SASL.Mechanism = sarama.SASLTypeSCRAMSHA256
	saramaConf.Net.TLS.Enable = true
	saramaConf.Net.TLS.Config = &tls.Config{
		InsecureSkipVerify: false,
	}
	kafkaVersion, err := sarama.ParseKafkaVersion(config.KafkaVersion)
	if err != nil {
		log.Printf("Error parsing KafkaVersion %s: %v", config.KafkaVersion, err)
	}
	saramaConf.Version = kafkaVersion
	return saramaConf
}

func getTopicWithPrefix(topic string, config *ClientConfig) string {
	return config.TopicPrefix + topic
}
