package auth

import "testing"

func TestStart(t *testing.T) {
	as := "Bearer 123456"
	expect := "123456"
	token := extractToken(as)
	if token != expect {
		t.Errorf("expected %s got %s", expect, token)
	}
}
