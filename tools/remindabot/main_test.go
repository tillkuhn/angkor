package main

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"testing"
	"time"
)

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
}

// http://www.inanzzz.com/index.php/post/fb0m/mocking-and-testing-http-clients-in-golang
func TestConsumer_GetReminders(t *testing.T) {
	srv := serverMock()
	defer srv.Close()

	resBody, err := fetchReminders(srv.URL + "/mock-api/reminders","12345","X-Auth-Token" )
	if err !=  nil {
		t.Error(err)
	}

	body, err := ioutil.ReadAll(resBody)
	if err !=  nil {
		t.Error(err)
	}
	resBody.Close()

	//if http.StatusOK != res.StatusCode  {
	//	t.Error("expected", http.StatusOK, "got", res.StatusCode)
	//}
	if `{"Notes":null,"Footer":"test"}` != string(body) {
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
		Notes:  nil,
		Footer: "test",
	}
	b,_:= json.Marshal(mockNotes)
	_, _ = w.Write(b)
}
