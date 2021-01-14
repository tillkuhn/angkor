package main

import (
	"bytes"
	"flag"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
	"html/template"
	"log"
	"net/mail"
	"os"
	"os/user"
	"path/filepath"
	"strings"
)

const appPrefix = "SMTP"

type Config struct {
	User     string `default:"eu-central-1" required:"true" desc:"User for SMTP Auth"`
	Password string `required:"true" desc:"Password for SMTP Auth"`
	Server   string `required:"true" desc:"SMTP Server w/o port"`
	Port     int    `default:"465" required:"true" desc:"SMTP(S) Server port"`
	Dryrun   bool   `default:"false"`
}

// SSL/TLS Email Example
// https://gist.github.com/chrisgillis/10888032
func main() {
	// Load .env from home
	usr, _ := user.Current()
	err := godotenv.Load(filepath.Join(usr.HomeDir, ".angkor", ".env"))
	if err != nil {
		log.Fatalf("Error loading .env file: %v", err)
	}

	// Help first
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	var config Config
	if *help {
		if err = envconfig.Usage(appPrefix, &config); err != nil {
			log.Fatalf("Erroring loading envconfig: %v",err)
		}
		os.Exit(0)
	}

	// Ready for Environment config, parse config based on Environment Variables
	err = envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatalf("Error init envconfig: %v", err)
	}

	testFrom := "remindabot@"+os.Getenv("CERTBOT_DOMAIN_NAME")
	testTo := strings.Replace(os.Getenv("CERTBOT_MAIL"), "@", "+ses@", 1)
	var buf bytes.Buffer
	tmpl,_ := template.New("").Parse(`<html><body><h3>🤖 Remindabot Report</h3><p>Hello <b>{{.}}</b></p></body></html>`)
	if err := tmpl.Execute(&buf, testTo); err != nil {
		log.Fatal(err)
	}
	mail := &Mail{From: mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"} ,
					To: mail.Address{Address: testTo},
					Subject: "Everybody rock your body", Body: buf.String()}
	sendmail(mail,config)

}
