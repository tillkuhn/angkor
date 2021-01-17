package main

import (
	"bytes"
	"encoding/json"
	"flag"
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

	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
)

const appPrefix = "SMTP"

// Config derrived from envconfig
type Config struct {
	User     string `default:"eu-central-1" required:"true" desc:"User for SMTP Auth"`
	Password string `required:"true" desc:"Password for SMTP Auth"`
	Server   string `required:"true" desc:"SMTP Server w/o port"`
	Port     int    `default:"465" required:"true" desc:"SMTP(S) Server port"`
	Dryrun   bool   `default:"false" desc:"Dryrun, dump mail to STDOUT instead of send"`
	APIUrl   string `default:"https://jsonplaceholder.typicode.com/users" desc:"REST API URL"`
}

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime string = "latest"
)

// SSL/TLS Email Example, based on https://gist.github.com/chrisgillis/10888032
func main() {
	log.Printf("starting service [%s] build %s with PID %d", path.Base(os.Args[0]), BuildTime, os.Getpid())

	// Load .env from home
	usr, _ := user.Current()
	for _, dir := range [...]string{".", usr.HomeDir, filepath.Join(usr.HomeDir, ".angkor")} {
		err := godotenv.Load(filepath.Join(dir, ".env"))
		if err == nil {
			log.Printf("Loading environment vars from %s", filepath.Join(dir, ".env"))
			break
		}
	}

	// Help first
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	var config Config
	if *help {
		if err := envconfig.Usage(appPrefix, &config); err != nil {
			log.Fatalf("Erroring loading envconfig: %v", err)
		}
		os.Exit(0)
	}

	// Ready for Environment config, parse config based on Environment Variables
	err := envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatalf("Error init envconfig: %v", err)
	}

	// Fetch reminders
	var myClient = &http.Client{Timeout: 10 * time.Second}
	log.Printf("Fetching notes from %s", config.APIUrl)
	r, err := myClient.Get(config.APIUrl)
	if err != nil {
		log.Fatalf("Error get %s: %v", config.APIUrl, err)
	}
	defer r.Body.Close()
	var notes []interface{} // should be concrete struct
	err = json.NewDecoder(r.Body).Decode(&notes)
	if err != nil {
		log.Fatalf("Error get %s", config.APIUrl, err)
	}

	// Prepare and send mail
	testFrom := "remindabot@" + os.Getenv("CERTBOT_DOMAIN_NAME")
	testTo := strings.Replace(os.Getenv("CERTBOT_MAIL"), "@", "+ses@", 1)
	var buf bytes.Buffer
	tmpl, _ := template.New("").Parse(Mailtemplate())
	if err := tmpl.Execute(&buf, &notes); err != nil {
		log.Fatal(err)
	}
	mail := &Mail{From: mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"},
		To:      mail.Address{Address: testTo},
		Subject: "Everybody rock your body", Body: buf.String()}
	sendmail(mail, config)

}
