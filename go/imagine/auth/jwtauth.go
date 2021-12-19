package auth

import (
	"errors"
	"fmt"
	"strings"

	"github.com/MicahParks/keyfunc"
	"github.com/golang-jwt/jwt/v4"
	"github.com/rs/zerolog/log"
)

// JwtAuth holds private members and provides functions to extract claims etc. from JWT
type JwtAuth struct {
	jwksEndpoint string
	jwks         *keyfunc.JWKS
}

// TokenInfo is a simple wrapper around JWT Claims with some useful methods
type TokenInfo struct {
	claims jwt.MapClaims
}

// NewJwtAuth should be initialized only once on startup, maybe add method to refresh JWKS later
func NewJwtAuth(jwksEndpoint string) (*JwtAuth, error) {
	// global logger is configured by main
	logger := log.Logger.With().Str("logger", "㊙️auth").Logger()

	logger.Info().Msgf("Downloading JSON Web Key Set (JWKS) from %s", jwksEndpoint)
	jwks, err := keyfunc.Get(jwksEndpoint, keyfunc.Options{})
	if err != nil || len(jwks.KIDs()) < 1 {
		errorMsg := fmt.Sprintf("Failed to get the JWKS from the given URL %s: func=%v error %v", jwksEndpoint, jwks, err)
		logger.Error().Err(err).Msg(errorMsg)
		return &JwtAuth{}, errors.New(errorMsg)
	}
	return &JwtAuth{jwksEndpoint, jwks}, nil
}

func (a JwtAuth) ParseClaims(authHeader string) (*TokenInfo, error) {
	jwtB64 := extractToken(authHeader)
	claims := jwt.MapClaims{}
	_, err := jwt.ParseWithClaims(jwtB64, claims, a.jwks.Keyfunc)
	return &TokenInfo{claims}, err
}

func (t *TokenInfo) Name() string {
	// Type Assertion to check if interface{} is a string, see https://stackoverflow.com/a/14289568/4292075
	if str, ok := t.claims["name"].(string); ok {
		return str
	} else {
		return ""
	}
}

func (t *TokenInfo) Scope() interface{} {
	return t.claims["scope"]
}

func (t *TokenInfo) Roles() []interface{} {
	if roles, ok := t.claims["cognito:roles"].([]interface{}); ok {
		return roles
	} else {
		return nil
	}
}

func (t *TokenInfo) Subject() interface{} {
	// Type Assertion to check if interface{} is a string, see https://stackoverflow.com/a/14289568/4292075
	if str, ok := t.claims["sub"].(string); ok {
		return str
	} else {
		return ""
	}
}

// extractToken returns the part after "Bearer " from the auth header
func extractToken(authHeader string) string {
	return strings.Split(authHeader, "Bearer ")[1]
}
