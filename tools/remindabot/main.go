package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/tillkuhn/angkor/tools/topkapi"
	"html/template"
	"log"
	"net/mail"
	"os"
	"path"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/dustin/go-humanize"
)

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
	Notes    []Note
	ImageUrl string
	Footer   string
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
	// Let me introduce myself - I'm Remindabot
	log.Printf("Starting service [%s] build=%s PID=%d OS=%s", path.Base(os.Args[0]), BuildTime, os.Getpid(), runtime.GOOS)

	// configure delegates most of the work to envconfig
	config := configure()

	reminderResponse, err := fetchReminders(config.ApiUrl, config.ApiToken, config.ApiTokenHeader)
	// for whatsoever reason this might fail the first time with timeout, so we retry once
	// err=Get ...: context deadline exceeded (Client.Timeout exceeded while awaiting headers)
	if err != nil {
		log.Printf("First fetch did not succeed: %s, trying one more time", err)
		reminderResponse, err = fetchReminders(config.ApiUrl, config.ApiToken, config.ApiTokenHeader)
		if err != nil {
			log.Printf("2nd Attemp also failed: %s, giving up", err)
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
		Notes:    notes,
		ImageUrl: config.ImageUrl,
		Footer:   mailFooter(),
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

	if config.KafkaSupport {
		log.Printf("Kafkaesk ....")
		// kafkaConf = topkapi.NewConfig()
		producer := topkapi.NewProducer(topkapi.NewConfig())
		defer producer.Close()
		producer.Publish([]byte("nur mal so"),"system")
	}
}

func mailSubject() string {
	now := time.Now()
	return fmt.Sprintf("Your friendly reminders for %s, %s %s %d", now.Weekday(), now.Month(), humanize.Ordinal(now.Day()), now.Year())
}

func mailFooter() string {
	rel := strings.Title(strings.Replace(ReleaseName, "-", " ", -1))
	year := time.Now().Year()
	return "&#169; " + strconv.Itoa(year) + " · Powered by Remindabot · " + AppVersion + " " + rel
}
