package main

import (
	"flag"
	"log"
	"os"
	"os/user"
	"path/filepath"

	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "Remindabot"

// Config derived from envConfig
type Config struct {
	SmtpUser       string `default:"eu-central-1" required:"true" desc:"SmtpUser for SMTP Auth" split_words:"true"`
	SmtpPassword   string `required:"true" desc:"SmtpPassword for SMTP Auth" split_words:"true"`
	SmtpServer     string `required:"true" desc:"SMTP SmtpServer w/o port" split_words:"true"`
	SmtpPort       int    `default:"465" required:"true" desc:"SMTP(S) SmtpServer port" split_words:"true"`
	SmtpDryrun     bool   `default:"false" desc:"SmtpDryrun, dump mail to STDOUT instead of send" split_words:"true"`
	ApiUrl         string `default:"http://localhost:8080/api/v1/notes/reminders" desc:"REST API URL" split_words:"true"`
	ApiTokenHeader string `default:"X-Auth-Token" desc:"HTTP Header for AuthToken" split_words:"true"`
	ApiToken       string `desc:"AuthToken value, if unset no header is sent" split_words:"true"` // REMINDABOT_API_TOKEN
	ImageUrl       string `desc:"Optional Image URL" split_words:"true" default:"https://timafe.files.wordpress.com/2015/12/img_3258_bike_pano2.jpg?w=948&h=202"`
}

func configure() *Config {
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
