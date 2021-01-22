package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"html/template"
	"log"
	"net/http"
	"net/mail"
	"os"
	"os/user"
	"path"
	"path/filepath"
	"strings"
	"time"

	"github.com/dustin/go-humanize"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "Remindabot"

// Config derrived from envconfig
type Config struct {
	SmtpUser       string `default:"eu-central-1" required:"true" desc:"SmtpUser for SMTP Auth" split_words:"true"`
	SmtpPassword   string `required:"true" desc:"SmtpPassword for SMTP Auth" split_words:"true"`
	SmtpServer     string `required:"true" desc:"SMTP SmtpServer w/o port" split_words:"true"`
	SmtpPort       int    `default:"465" required:"true" desc:"SMTP(S) SmtpServer port" split_words:"true"`
	SmtpDryrun     bool   `default:"false" desc:"SmtpDryrun, dump mail to STDOUT instead of send" split_words:"true"`
	ApiUrl         string `default:"http://localhost:8080/api/v1/notes/reminders" desc:"REST API URL" split_words:"true"`
	ApiTokenHeader string `default:"X-Auth-Token" desc:"HTTP Header for AuthToken" split_words:"true"`
	ApiToken       string `desc:"AuthToken value, if unset no header is sent" split_words:"true"` // REMINDABOT_API_TOKEN
}

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime string = "latest"
)

// SSL/TLS Email Example, based on https://gist.github.com/chrisgillis/10888032
func main() {
	log.Printf("starting service [%s] build %s with PID %d", path.Base(os.Args[0]), BuildTime, os.Getpid())

	// Help first
	var config Config
	var help = flag.Bool("h", false, "display help message")
	var envfile = flag.String("envfile", "", "location of environment variable file e.g. /tmp/.env")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		if err := envconfig.Usage(appPrefix, &config); err != nil {
			log.Fatalf("Erroring loading envconfig: %v", err)
		}
		os.Exit(0)
	}
	if *envfile != "" {
		log.Printf("Loading environment from custom location %s", *envfile)
		err := godotenv.Load(*envfile)
		if err != nil {
			log.Fatalf("Error Loading environment vars from %s: %v", *envfile, err)
		}
	} else {
		// Load .env from home
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

	// Fetch reminders
	var myClient = &http.Client{Timeout: 10 * time.Second}
	log.Printf("Fetching notes from %s", config.ApiUrl)
	req, _ := http.NewRequest("GET", config.ApiUrl, nil)
	if config.ApiToken != "" {
		req.Header.Set(config.ApiTokenHeader, config.ApiToken)
	}
	r, err := myClient.Do(req)
	if r == nil || r.StatusCode < 200 || r.StatusCode >= 300 {
		log.Fatalf("Error retrieving %s: response=%v", config.ApiUrl, r)
	}
	if r.StatusCode >= 400 {
		log.Fatalf("Error retrieving %s: status=%d", config.ApiUrl, r.StatusCode)
	}
	defer r.Body.Close()
	var notes []interface{} // should be concrete struct
	err = json.NewDecoder(r.Body).Decode(&notes)
	if err != nil {
		log.Fatalf("Error get %s: %v", config.ApiUrl, err)
	}

	// Prepare and send mail
	testFrom := "remindabot@" + os.Getenv("CERTBOT_DOMAIN_NAME")
	testTo := strings.Replace(os.Getenv("CERTBOT_MAIL"), "@", "+ses@", 1)
	var buf bytes.Buffer
	tmpl, _ := template.New("").Parse(Mailtemplate())
	if err := tmpl.Execute(&buf, &notes); err != nil {
		log.Fatal(err)
	}
	mail := &Mail{
		From:    mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"},
		To:      mail.Address{Address: testTo},
		Subject: mailSubject(),
		Body:    buf.String(),
	}
	Sendmail(mail, config)

}

func mailSubject() string {
	now := time.Now()
	return fmt.Sprintf("Your friendly reminder report for %s, %s %s %d", now.Weekday(), now.Month(), humanize.Ordinal(now.Day()), now.Year())
}
