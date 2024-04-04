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
	authContextDisabled = New(false, "", "4711")
	authContextEnabled  = New(true, "", "4711")
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
	authContextDisabled.ValidationMiddleware(testHandler).ServeHTTP(rr, req)
	assert.Equal(t, rr.Code, http.StatusOK, rr.Body.String())
	assert.Contains(t, rr.Body.String(), "Test OK")

}

func TestValidTokenInvalidMiddleware(t *testing.T) {
	rr := httptest.NewRecorder()
	req, _ := http.NewRequest("POST", "/sandbox/can-i-upload.txt", nil)
	testHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})
	// encToken, _ := issueToken("extra-protected")
	// both headers should be checked
	for _, ah := range []string{"X-Authorization", "Authorization"} {
		req.Header.Set(ah, "Bearer invalid-string")
		authContextEnabled.ValidationMiddleware(testHandler).ServeHTTP(rr, req)
		assert.Equal(t, http.StatusForbidden, rr.Code, rr.Body.String())
		assert.Contains(t, strings.ToLower(rr.Body.String()), "invalid number of segments")
	}
}

func TestSimpleTokenInvalidMiddleware(t *testing.T) {
	rr := httptest.NewRecorder()
	req, _ := http.NewRequest("POST", "/metrics", nil)
	testHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})
	// encToken, 	_ := issueToken("extra-protected")
	// both headers should be checked
	validToken := "hello-bear"
	for _, ah := range []string{"X-Authorization"} {
		req.Header.Set(ah, "Bearer "+validToken)
		authContextEnabled.CheckTokenMiddleware(validToken, testHandler).ServeHTTP(rr, req)
		assert.Equal(t, http.StatusOK, rr.Code, rr.Body.String())
		req.Header.Set(ah, "Bearer thisIsAnInvalidToken")
		authContextEnabled.CheckTokenMiddleware(validToken, testHandler).ServeHTTP(rr, req)
		assert.Equal(t, http.StatusForbidden, rr.Code, rr.Body.String())
		assert.Contains(t, strings.ToLower(rr.Body.String()), "doesn't match expected t")
	}
}
