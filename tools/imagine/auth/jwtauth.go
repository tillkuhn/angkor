package auth

import (
	"errors"
	"fmt"
	"github.com/MicahParks/keyfunc"
	"github.com/golang-jwt/jwt/v4"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"os"
	"strings"
)

// JwtAuth holds private members and provides functions to extract claims etc. from JWT
type JwtAuth struct {
	jwksEndpoint string
	jwks *keyfunc.JWKs
}

type JwtToken struct {
	claims jwt.MapClaims
}

// NewJwtAuth should be initialized only once on startup, maybe add method to refresh JWKS later
func NewJwtAuth(jwksEndpoint string) (*JwtAuth, error) {
	logger := log.Output(zerolog.ConsoleWriter{Out: os.Stderr}).With().Str("logger", "㊙️auth").Logger()

	logger.Info().Msgf("Downloading JSON Web Key Set (JWKS) from %s", jwksEndpoint)
	jwks, err := keyfunc.Get(jwksEndpoint)
	if err != nil || len(jwks.KIDs()) < 1 {
		errorMsg := fmt.Sprintf("Failed to get the JWKS from the given URL %s: func=%v error %v", jwksEndpoint, jwks, err)
		logger.Error().Err(err).Msg(errorMsg)
		return &JwtAuth{}, errors.New(errorMsg)
	}
	return &JwtAuth{jwksEndpoint, jwks}, nil
}

func (a JwtAuth) ParseClaims(authHeader string) (*JwtToken, error) {
	jwtB64 := extractToken(authHeader)
	claims := jwt.MapClaims{}
	_, err := jwt.ParseWithClaims(jwtB64, claims,a.jwks.Keyfunc)
	return &JwtToken{claims}, err
}

func (t JwtToken) Name() string {
	// Type Assertion to check if interface{} is a string, see https://stackoverflow.com/a/14289568/4292075
	if str, ok := t.claims["name"].(string); ok {
		return str
	} else {
		return ""
	}
}

func (t JwtToken) Scope() interface{} {
	return t.claims["scope"]
}

func (t JwtToken) Roles() []interface{} {
	if roles, ok := t.claims["cognito:roles"].([]interface{}); ok {
		return roles
	} else {
		return nil
	}
}

func (t JwtToken) Subject() interface{} {
	// Type Assertion to check if interface{} is a string, see https://stackoverflow.com/a/14289568/4292075
	if str, ok := t.claims["sub"].(string); ok {
		return str
	} else {
		return ""
	}
}

func extractToken(authHeader string) string {
	return strings.Split(authHeader, "Bearer ")[1]
}
