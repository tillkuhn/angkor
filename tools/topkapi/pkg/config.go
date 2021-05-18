package pkg

import (
	"flag"
	"log"
	"os"
	"os/user"
	"path/filepath"

	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "Kafka"

// Config derived from envConfig
type Config struct {
	Brokers       string `required:"true" desc:"Comma separated List of brokers" split_words:"true"`
	SaslUsername  string `required:"true" desc:"User for SASL Auth" split_words:"true"`
	SaslPassword  string `required:"true" desc:"Password for SASL Auth" split_words:"true"`
	SaslMechanism string `default:"SCRAM-SHA-256" required:"true" desc:"SASL Mechanism" split_words:"true"`
	TlsEnabled    bool   `default:"true" desc:"TLS Encryption active" split_words:"true"`
	SaslEnabled   bool   `default:"true" desc:"Use SASL Authentication" split_words:"true"`
}

func NewConfig() *Config {
	// Check first if people need helpRequired
	var config Config
	var helpRequired = flag.Bool("h", false, "display helpRequired message")
	var envFile = flag.String("envfile", "", "location of environment variable file e.g. /tmp/.env")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *helpRequired {
		exitCode := 0
		if err := envconfig.Usage(appPrefix, &config); err != nil {
			exitCode = 1
			log.Fatalf("Erroring loading envconfig: %v", err)
		}
		os.Exit(exitCode)
	}
	if *envFile != "" {
		log.Printf("Loading environment from custom location %s", *envFile)
		err := godotenv.Load(*envFile)
		if err != nil {
			log.Fatalf("Error Loading environment vars from %s: %v", *envFile, err)
		}
	} else {
		// env file not specified, try user home dir and ~/.angkor
		usr, _ := user.Current()
		for _, dir := range [...]string{".", usr.HomeDir, filepath.Join(usr.HomeDir, ".angkor")} {
			err := godotenv.Load(filepath.Join(dir, ".env"))
			if err == nil {
				log.Printf("Loading environment vars from %s", filepath.Join(dir, ".env"))
				break
			}
		}
	}

	// Ready for Environment config, parse config based on Environment Variables
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatalf("Error init envconfig: %v", err)
	}

	return &config
}
