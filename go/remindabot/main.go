package main

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"github.com/cloudevents/sdk-go/v2/event"
	"github.com/segmentio/kafka-go"
	"github.com/tillkuhn/rubin/pkg/polly"
	"html/template"
	"io"
	"log"
	"net/mail"
	"os"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/tillkuhn/rubin/pkg/rubin"
	"golang.org/x/text/cases"
	"golang.org/x/text/language"

	"github.com/dustin/go-humanize"
	"github.com/tillkuhn/angkor/go/topkapi"
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
	Events   []event.Event
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
	// kafka topic for app events
	topic = "app.events"
)

// SSL/TLS Email Example, based on https://gist.github.com/chrisgillis/10888032
func main() {
	// Let me introduce myself - I'm Remindabot
	startMsg := fmt.Sprintf("Starting service [%s] build=%s PID=%d OS=%s", AppId, BuildTime, os.Getpid(), runtime.GOOS)
	logger.Println(startMsg)

	// Configure delegates most of the work to envconfig
	config := configure()

	// Kafka event support
	kClient := topkapi.NewClientWithId(AppId)
	defer kClient.Close()
	kClient.Enable(config.KafkaSupport) // suppress events
	if _, _, err := kClient.PublishEvent(kClient.NewEvent("runjob:"+AppId, startMsg), "system"); err != nil {
		logger.Fatalf("Error publish event to %s: %v", "system", err)
	}

	// Experiment with new Kafka Setup (rubin)
	payload := map[string]string{"app": AppId, "version": AppVersion, "status": "started"}
	rc := rubin.Must[*rubin.Client](rubin.NewClientFromEnv())
	// fmt.Println(event)
	if resp, err := rc.Produce(context.Background(), rubin.RecordRequest{
		Topic:        topic,
		Data:         payload,
		AsCloudEvent: true,
		Source:       "urn:app:angkor/" + AppId,
		Type:         "net.timafe.event.app.started.v1",
		Subject:      "reminder",
		// Headers: map[string]string{}, // can be nil
	}); err != nil {
		logger.Printf("push message to %s failed: %v", topic, err) // but continue
	} else {
		logger.Printf("successfully pushed message to %s: %d", topic, resp.ErrorCode)
	}

	// let's get back to our actual business
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

	defer func(reminderResponse io.ReadCloser) {
		_ = reminderResponse.Close()
	}(reminderResponse)
	// Parse JSON body into a list of notes
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

	// with i,note syntax, note would just be a copy, use index to access the actual list item
	// Ref.: https://yourbasic.org/golang/gotcha-change-value-range/
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

	// Pull recent events from topic to create a digest
	// actions := make(map[string]int)
	// kClient.Config.ConsumerTimeout = 5 * time.Second
	// consumeEvents(kClient, actions)
	// logger.Printf("Received actions %v", actions)
	p, err := polly.NewClientFromEnv()
	logger.Printf("Starting polly client %v to check for new events", p)
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*10)
	defer cancel()
	var events []event.Event
	topics := []string{"ci.events", "app.events", "system.events"}
	err = p.Poll(ctx,
		kafka.ReaderConfig{GroupTopics: topics, GroupID: config.KafkaGroupID},
		func(_ context.Context, m kafka.Message) {
			if ce, err := polly.AsCloudEvent(m); err == nil {
				logger.Printf("Received event %s %s %s", ce.Source(), ce.Type(), ce.Subject())
				events = append(events, ce)
			} else {
				logger.Printf("WARNING: Message %v cannot be parsed into an event", m)
			}
		})
	logger.Printf("Received %d events", len(events))
	p.WaitForClose()

	// Prepare and send reminderMail
	testFrom := "remindabot@" + os.Getenv("CERTBOT_DOMAIN_NAME")
	testTo := strings.Replace(os.Getenv("CERTBOT_MAIL"), "@", "+ses@", 1)
	var buf bytes.Buffer
	tmpl, _ := template.New("").Parse(mailTemplate())
	noteMailBody := &NoteMailBody{
		Notes:    notes,
		Events:   events,
		ImageUrl: config.ImageUrl,
		Footer:   mailFooter(),
	}

	// render the mail body
	if err := tmpl.Execute(&buf, &noteMailBody); err != nil {
		logger.Fatal(err)
	}

	// Compose mail structure and send it
	reminderMail := &Mail{
		From:    mail.Address{Address: testFrom, Name: "TiMaFe Remindabot"},
		To:      mail.Address{Address: testTo},
		Subject: mailSubject(),
		Body:    buf.String(),
	}
	sendMail(reminderMail, config)

	msg := fmt.Sprintf("Sent mail with %d due notes", len(notes))
	if _, _, err := kClient.PublishEvent(kClient.NewEvent("create:mail", msg), "system"); err != nil {
		logger.Fatalf("Error publish event to %s: %v", "system", err)
	}
}

func mailSubject() string {
	now := time.Now()
	return fmt.Sprintf("Your friendly reminders for %s, %s %s %d", now.Weekday(), now.Month(), humanize.Ordinal(now.Day()), now.Year())
}

func mailFooter() string {
	cs := cases.Title(language.English)
	rel := cs.String(strings.Replace(ReleaseName, "-", " ", -1))
	year := time.Now().Year()
	return "&#169; " + strconv.Itoa(year) + " · Powered by Remindabot · " + AppVersion + " " + rel
}
