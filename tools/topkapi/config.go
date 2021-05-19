package topkapi

import (
	"log"
	"os/user"
	"path/filepath"

	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
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
