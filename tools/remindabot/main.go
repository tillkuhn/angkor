package main

import (
	"flag"
	"github.com/joho/godotenv"
	"github.com/kelseyhightower/envconfig"
	"crypto/tls"
	"fmt"
	"log"
	"net/mail"
	"net/smtp"
	"os"
	"os/user"
	"path/filepath"
	"strings"
)

const appPrefix = "SMTP"

type Config struct {
	User string `default:"eu-central-1" required:"true" desc:"User for SMTP Auth`
	Password  string `required:"true" desc:"Password for SMTP Auth"`
	Server string `required:"true" desc:"SMTP Server w/o port""`
	Port int `default:"465"required:"true" desc:"SMTP Server port""`
}

var (
	config      Config
)
// SSL/TLS Email Example

func main() {
	// Help first
	var help = flag.Bool("h", false, "display help message")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		envconfig.Usage(appPrefix, &config)
		os.Exit(0)
	}

	// Load .env from home
	usr, _ := user.Current()
	err := godotenv.Load(filepath.Join(usr.HomeDir,".angkor",".env"))
	if err != nil {
		log.Fatalf("Error loading .env file: %v",err)
	}

	// Ready for Envconfig
	// Parse config based on Environment Variables
	err = envconfig.Process(appPrefix, &config)
	if err != nil {
		log.Fatalf("Error init envconfig: %v",err)
	}


	log.Printf("Sending mail via %s:%d", config.Server,config.Port)
	testmail := strings.Replace(os.Getenv("CERTBOT_MAIL"),"@","+ses@",1)
	from := mail.Address{"", testmail} // only for testing
	to := mail.Address{"", testmail}
	subj := "Everybody rock your body"
	body := "This is an example body.\n With two lines."

	// Setup headers
	headers := make(map[string]string)
	headers["From"] = from.String()
	headers["To"] = to.String()
	headers["Subject"] = subj

	// Setup message
	message := "Hallo Hase alles klar"
	for k, v := range headers {
		message += fmt.Sprintf("%s: %s\r\n", k, v)
	}
	message += "\r\n" + body

	// Connect to the SMTP Server


	auth := smtp.PlainAuth("", config.User, config.Password, config.Server)

	// TLS config
	tlsconfig := &tls.Config{
		InsecureSkipVerify: true,
		ServerName:         config.Server,
	}

	// Here is the key, you need to call tls.Dial instead of smtp.Dial
	// for smtp servers running on 465 that require an ssl connection
	// from the very beginning (no starttls)
	conn, err := tls.Dial("tcp", fmt.Sprintf("%s:%d",config.Server,config.Port), tlsconfig)
	if err != nil {
		log.Panic(err)
	}

	c, err := smtp.NewClient(conn, config.Server)
	if err != nil {
		log.Panic(err)
	}

	// Auth
	if err = c.Auth(auth); err != nil {
		log.Panic(err)
	}

	// To && From
	if err = c.Mail(from.Address); err != nil {
		log.Panic(err)
	}

	if err = c.Rcpt(to.Address); err != nil {
		log.Panic(err)
	}

	// Data
	w, err := c.Data()
	if err != nil {
		log.Panic(err)
	}

	_, err = w.Write([]byte(message))
	if err != nil {
		log.Panic(err)
	}

	err = w.Close()
	if err != nil {
		log.Panic(err)
	}

	c.Quit()

}
