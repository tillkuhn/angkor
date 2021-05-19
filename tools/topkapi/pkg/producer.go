package pkg

// Based on https://github.com/Shopify/sarama/tree/master/examples/sasl_scram_client
import (
	"crypto/tls"
	"flag"
	"log"
	"os"
	"strings"

	"github.com/Shopify/sarama"
)

func init() {
	sarama.Logger = log.New(os.Stdout, "[Sarama] ", log.LstdFlags)
}

var (
	algorithm = flag.String("algorithm", "sha256", "The SASL SCRAM SHA algorithm sha256 or sha512 as mechanism")
	useTLS    = flag.Bool("tls", true, "Use TLS to communicate with the cluster")
	logger = log.New(os.Stdout, "[Producer] ", log.LstdFlags)
)


func Publish(message []byte, topic string,  config *Config) {
	flag.Parse()

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
	if *algorithm == "sha512" {
		conf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA512} }
		conf.Net.SASL.Mechanism = sarama.SASLTypeSCRAMSHA512
	} else if *algorithm == "sha256" {
		conf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA256} }
		conf.Net.SASL.Mechanism = sarama.SASLTypeSCRAMSHA256
	} else {
		log.Fatalf("invalid SHA algorithm \"%s\": can be either \"sha256\" or \"sha512\"", *algorithm)
	}

	if *useTLS {
		conf.Net.TLS.Enable = true
		conf.Net.TLS.Config = &tls.Config{
			InsecureSkipVerify: false,
		}
	}

	syncProducer, err := sarama.NewSyncProducer(splitBrokers, conf)
	if err != nil {
		logger.Fatalln("failed to create producer: ", err)
	}
	partition, offset, err := syncProducer.SendMessage(&sarama.ProducerMessage{
		Topic: topic,
		Value: sarama.ByteEncoder(message),
	})
	if err != nil {
		logger.Fatalln("failed to send message to ", topic, err)
	}
	logger.Printf("wrote message at partition: %d, offset: %d", partition, offset)
	_ = syncProducer.Close()
	logger.Println("Bye now !")
}

