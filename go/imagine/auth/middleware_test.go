package auth

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

var (
	// expiredToken = fmt.Sprintf("%s.%s.%s", "eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp...",
	authContextDisabled = NewHandlerContext(false, "", "4711")
	authContextEnabled  = NewHandlerContext(true, "", "4711")
)

func TestValidTokenMiddlewareSecurityDisabled(t *testing.T) {
	rr := httptest.NewRecorder()
	req, err := http.NewRequest("POST", "/songs/test.mp3", nil)
	if err != nil {
		t.Fatal(err)
	}
	testHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("Test OK"))
	},
	)
	// encToken, _ := issueToken(validPath)
	// req.Header.Set("Authorization", "Bearer "+encToken)
	authContextDisabled.AuthValidationMiddleware(testHandler).ServeHTTP(rr, req)
	assert.Equal(t, rr.Code, http.StatusOK, rr.Body.String())
	assert.Contains(t, rr.Body.String(), "Test OK")

}

func TestValidTokenInvalidMiddleware(t *testing.T) {
	rr := httptest.NewRecorder()
	req, _ := http.NewRequest("POST", "/sandbox/can-i-upload.txt", nil)
	testHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})
	// encToken, _ := issueToken("extra-protected")
	req.Header.Set("X-Authorization", "Bearer invalid-string")
	authContextEnabled.AuthValidationMiddleware(testHandler).ServeHTTP(rr, req)
	assert.Equal(t, rr.Code, http.StatusForbidden, rr.Body.String())
	assert.Contains(t, strings.ToLower(rr.Body.String()), "invalid number of segments")
}
