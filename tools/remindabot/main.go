package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"html/template"
	"log"
	"net/mail"
	"os"
	"os/user"
	"path"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/dustin/go-humanize"
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
}

// Note represents a "Reminder" note from API
type Note struct {
	Tags          []string    `json:"tags"`
	ID            string      `json:"id"`
	Status        string      `json:"status"`
	Summary       string      `json:"summary"`
	PrimaryURL    interface{} `json:"primaryUrl"`
	CreatedAt     interface{} `json:"createdAt"`
	AuthScope     string      `json:"authScope"`
	DueDate       string      `json:"dueDate"`
	DueDateHuman  string      `json:"string"`
	UserName      string      `json:"userName"`
	UserEmail     string      `json:"userEmail"`
	UserShortName string      `json:"userShortName"`
	NoteUrl       string      `json:"noteUrl"`
}

// NoteMailBody wraps a list of Notes plus an optional footer to compose the outbound mail
type NoteMailBody struct {
	Notes  []Note
	Footer string
}

var (
	// BuildTime will be overwritten by ldflags, e.g. -X 'main.BuildTime=...
	BuildTime = "now"
	// AppVersion should follow semver
	AppVersion = "latest"
	// ReleaseName can be anything nice
	ReleaseName = "pura-vida"
)

// SSL/TLS Email Example, based on https://gist.github.com/chrisgillis/10888032
func main() {
	log.Printf("Starting service [%s] build=%s PID=%d OS=%s", path.Base(os.Args[0]), BuildTime, os.Getpid(), runtime.GOOS)

	// Help first
	var config Config
	var help = flag.Bool("h", false, "display help message")
	var envFile = flag.String("envFile", "", "location of environment variable file e.g. /tmp/.env")
	flag.Parse() // call after all flags are defined and before flags are accessed by the program
	if *help {
		if err := envconfig.Usage(appPrefix, &config); err != nil {
			log.Fatalf("Erroring loading envconfig: %v", err)
		}
		os.Exit(0)
	}
	if *envFile != "" {
		log.Printf("Loading environment from custom location %s", *envFile)
		err := godotenv.Load(*envFile)
		if err != nil {
			log.Fatalf("Error Loading environment vars from %s: %v", *envFile, err)
		}
	} else {
		// try user home dir
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

	reminderResponse,err := fetchReminders(config.ApiUrl,config.ApiToken,config.ApiTokenHeader)
	if err != nil {
		log.Printf("First fetch did not succeed: %s, trying one more time",err)
		reminderResponse,err = fetchReminders(config.ApiUrl,config.ApiToken,config.ApiTokenHeader)
		if err != nil {
			log.Printf("2nd Attemp also failed: %s, giving up",err)
		}
	}

	defer reminderResponse.Close()
	// Parse JSON body into a lit of notes
	var notes []Note // var notes []interface{} is now a concrete struct
	err = json.NewDecoder(reminderResponse).Decode(&notes)
	if err != nil {
		log.Fatalf("Error get %s: %v", config.ApiUrl, err)
	}

	// we only send out a reminderMail if we have at least one reminder to notify
	if len(notes) < 1 {
		log.Printf("WARNING: No notes due today - we should call it a day and won't sent out any reminderMail")
		return
	}

	// with i,n n would just be a copy, use index to access the actual list item https://yourbasic.org/golang/gotcha-change-value-range/
	for i := range notes {
		if strings.Contains(notes[i].UserName, " ") {
			names := strings.Split(notes[i].UserName, " ")
			if len(names[1]) >= 1 {
				notes[i].UserShortName = fmt.Sprintf("%s %s.", names[0], names[1][0:1])
			} else {
				notes[i].UserShortName = names[0]
			}
		} else {
			notes[i].UserShortName = notes[i].UserName
		}
		myDate, dateErr := time.Parse("2006-01-02", notes[i].DueDate)
		if dateErr != nil {
			fmt.Printf("WARN: Cannot parse date %s: %v ", notes[i].DueDate, dateErr)
		} else {
			notes[i].DueDateHuman = humanize.Time(myDate)
		}
	}

	// Prepare and send reminderMail
	testFrom := "remindabot@" + os.Getenv("CERTBOT_DOMAIN_NAME")
	testTo := strings.Replace(os.Getenv("CERTBOT_MAIL"), "@", "+ses@", 1)
	var buf bytes.Buffer
	tmpl, _ := template.New("").Parse(mailTemplate())
	noteMailBody := &NoteMailBody{
		Notes:  notes,
		Footer: mailFooter(),
	}

	if err := tmpl.Execute(&buf, &noteMailBody); err != nil {
		log.Fatal(err)
	}
	reminderMail := &Mail{
		From:    mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"},
		To:      mail.Address{Address: testTo},
		Subject: mailSubject(),
		Body:    buf.String(),
	}
	sendMail(reminderMail, config)
}

func mailSubject() string {
	now := time.Now()
	return fmt.Sprintf("Your friendly reminders for %s, %s %s %d", now.Weekday(), now.Month(), humanize.Ordinal(now.Day()), now.Year())
}

func mailFooter() string {
	rel := strings.Title(strings.Replace(ReleaseName, "-", " ", -1))
	year := time.Now().Year()
	return "&#169; " + strconv.Itoa(year) + " · Powered by Remindabot · "+AppVersion+" " + rel
}
