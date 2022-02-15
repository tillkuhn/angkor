package auth

import (
	"errors"
	"fmt"
	"strings"

	"github.com/MicahParks/keyfunc"
	"github.com/golang-jwt/jwt/v4"
	"github.com/rs/zerolog/log"
)

// JwtAuth holds private members for the jwks endpoint and the key function,
// and provides a ParseClaim method
type JwtAuth struct {
	jwksEndpoint string
	jwks         *keyfunc.JWKS
}

// NewJwtAuth should be initialized only once on startup, maybe add method to refresh JWKS later
func NewJwtAuth(jwksEndpoint string) (*JwtAuth, error) {
	// global logger is configured by main
	logger := log.Logger.With().Str("logger", "㊙️auth").Logger()

	logger.Info().Msgf("[AUTH] Downloading JSON Web Key Set (JWKS) from %s", jwksEndpoint)
	jwks, err := keyfunc.Get(jwksEndpoint, keyfunc.Options{})
	if err != nil || len(jwks.KIDs()) < 1 {
		errorMsg := fmt.Sprintf("Failed to get the JWKS from the given URL %s: func=%v error %v", jwksEndpoint, jwks, err)
		logger.Error().Err(err).Msg(errorMsg)
		return &JwtAuth{}, errors.New(errorMsg)
	}
	return &JwtAuth{jwksEndpoint, jwks}, nil
}

// TokenInfo is a simple wrapper around JWT Claims with some useful methods
type TokenInfo struct {
	claims jwt.MapClaims
}

// ParseClaims Parse parses, validates, verifies the signature
// and returns a TokenInfo pointer wrapping the parsed claims
func (a JwtAuth) ParseClaims(authHeader string) (*TokenInfo, error) {
	jwtB64 := extractToken(authHeader)
	claims := jwt.MapClaims{}
	_, err := jwt.ParseWithClaims(jwtB64, claims, a.jwks.Keyfunc)
	return &TokenInfo{claims}, err
}

// Name returns the claim "name", or empty string if not defined (or not compatible with string)
func (t *TokenInfo) Name() string {
	// Type Assertion to check if interface{} is a string, see https://stackoverflow.com/a/14289568/4292075
	if str, ok := t.claims["name"].(string); ok {
		return str
	} else {
		return ""
	}
}

// Scope returns the claim "scope",
// In Cognito, this is the User's ID e.g. "39134950-97ef-4961-a4b1-96********"
func (t *TokenInfo) Scope() interface{} {
	return t.claims["scope"]
}

// Roles returns the Cognito specific roles claim "cognito:roles"
//   "cognito:roles": [
//      "arn:aws:iam::06**********:role/*******-cognito-role-user",
//      "arn:aws:iam::06**********:role/*******-cognito-role-admin"
//    ]
func (t *TokenInfo) Roles() []interface{} {
	if roles, ok := t.claims["cognito:roles"].([]interface{}); ok {
		return roles
	} else {
		return nil
	}
}

// Subject returns the claim "sub", or empty string if not present
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
