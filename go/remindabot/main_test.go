package main

import (
	"encoding/json"
	"io"
	"net/http"
	"net/http/httptest"
	"os"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/cdfmlr/ellipsis"
	"github.com/cloudevents/sdk-go/v2/event"

	"github.com/stretchr/testify/assert"
)

func TestConfig(t *testing.T) {
	os.Clearenv()
	_ = os.Setenv("REMINDABOT_SMTP_USER", "harry")
	_ = os.Setenv("REMINDABOT_SMTP_PASSWORD", "use-chats")
	_ = os.Setenv("REMINDABOT_SMTP_SERVER", "mock.mail.com")
	_ = os.Setenv("REMINDABOT_SMTP_PORT", "999")
	_ = os.Setenv("REMINDABOT_API_TOKEN", "123456789012")
	c := configure()
	if c.SmtpPort != 999 {
		t.Errorf("expected %v, got %v", 999, c.SmtpPort)
	}
	if c.SmtpUser != "harry" {
		t.Errorf("expected %v, got %v", "harry", c.SmtpUser)
	}
	if c.SmtpServer != "mock.mail.com" {
		t.Errorf("expected %v, got %v", "mock.mail.com", c.SmtpServer)
	}
	if c.SmtpPassword != "use-chats" {
		t.Errorf("expected %v, got %v", "use-chats", c.SmtpPassword)
	}
	if c.ApiToken != "123456789012" {
		t.Errorf("expected %v, got %v", "123456789012", c.ApiToken)
	}
}

func TestEllipsis(t *testing.T) {
	str := "net.timafe.event.system.renew-cert.v1"
	assert.Equal(t, "....system.renew-cert.v1", ellipsis.Starting(str, 24))
}
func TestAddTagJPEG(t *testing.T) {
	actual := mailSubject()
	wd := time.Now().Weekday().String()
	if !strings.Contains(actual, wd) {
		t.Errorf("Subject test failed expected '%s' to contain '%s'", actual, wd)
	}
}

func TestFooter(t *testing.T) {
	actual := mailFooter()
	year := time.Now().Year()
	rel := "atest"
	if !strings.Contains(actual, rel) || !strings.Contains(actual, strconv.Itoa(year)) {
		t.Errorf("Subject test failed expected '%s' to contain '%d' and '%s'", actual, year, rel)
	}
	assert.Contains(t, actual, "Pura Vida")
}

// http://www.inanzzz.com/index.php/post/fb0m/mocking-and-testing-http-clients-in-golang
func TestConsumer_GetReminders(t *testing.T) {
	srv := serverMock()
	defer srv.Close()

	resBody, err := fetchReminders(srv.URL+"/mock-api/reminders", "12345", "X-Auth-Token")
	if err != nil {
		t.Error(err)
	}

	body, err := io.ReadAll(resBody)
	if err != nil {
		t.Error(err)
	}
	_ = resBody.Close()

	//if http.StatusOK != res.StatusCode  {
	//	t.Error("expected", http.StatusOK, "got", res.StatusCode)
	//}
	if `{"Notes":null,"Events":[],"ImageUrl":"","Footer":"test"}` != string(body) {
		t.Error("expected mock server responding got", string(body))
	}
}

func serverMock() *httptest.Server {
	handler := http.NewServeMux()
	handler.HandleFunc("/mock-api/reminders", notesMock)
	srv := httptest.NewServer(handler)
	return srv
}

func notesMock(w http.ResponseWriter, _ *http.Request) {
	mockNotes := NoteMailBody{
		Notes:    nil,
		Events:   []event.Event{},
		ImageUrl: "",
		Footer:   "test",
	}
	b, _ := json.Marshal(mockNotes)
	_, _ = w.Write(b)
}
