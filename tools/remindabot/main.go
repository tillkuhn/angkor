package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"html/template"
	"log"
	"net/mail"
	"os"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/tillkuhn/angkor/tools/topkapi"

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
	AppId       = "remindabot"
	logger      = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)
)

// SSL/TLS Email Example, based on https://gist.github.com/chrisgillis/10888032
func main() {
	// Let me introduce myself - I'm Remindabot
	startMsg := fmt.Sprintf("Starting service [%s] build=%s PID=%d OS=%s", AppId, BuildTime, os.Getpid(), runtime.GOOS)
	logger.Println(startMsg)

	// configure delegates most of the work to envconfig
	config := configure()

	// Kafka event support
	client := topkapi.NewClient()
	defer client.Close()
	if !config.KafkaSupport {
		client.Disable() // suppress events
	}
	if _, _, err := client.PublishEvent(createEvent("start:"+AppId, startMsg), "system"); err != nil {
		logger.Fatalf("Error publish event to %s: %v", "system", err)
	}

	reminderResponse, err := fetchReminders(config.ApiUrl, config.ApiToken, config.ApiTokenHeader)
	// for whatsoever reason this might fail the first time with timeout, so we retry once
	// err=Get ...: context deadline exceeded (Client.Timeout exceeded while awaiting headers)
	if err != nil {
		logger.Printf("First fetch did not succeed: %s, trying one more time", err)
		reminderResponse, err = fetchReminders(config.ApiUrl, config.ApiToken, config.ApiTokenHeader)
		if err != nil {
			logger.Printf("2nd Attempt also failed: %s, giving up", err)
		}
	}

	defer reminderResponse.Close()
	// Parse JSON body into a lit of notes
	var notes []Note // var notes []interface{} is now a concrete struct
	err = json.NewDecoder(reminderResponse).Decode(&notes)
	if err != nil {
		logger.Fatalf("Error get %s: %v", config.ApiUrl, err)
	}

	// we only send out a reminderMail if we have at least one reminder to notify
	if len(notes) < 1 {
		logger.Printf("WARNING: No notes due today - we should call it a day and won't sent out any reminderMail")
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
		logger.Fatal(err)
	}
	reminderMail := &Mail{
		From:    mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"},
		To:      mail.Address{Address: testTo},
		Subject: mailSubject(),
		Body:    buf.String(),
	}
	sendMail(reminderMail, config)

	msg := fmt.Sprintf("%d notes have been remindered", len(notes))
	if _, _, err := client.PublishEvent(createEvent("send:reminder", msg), "system"); err != nil {
		logger.Fatalf("Error publish event to %s: %v", "system", err)
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

func createEvent(action string, message string) *topkapi.Event {
	return &topkapi.Event{
		Source:  AppId,
		Time:    time.Now(),
		Action:  action,
		Message: message,
	}
}
