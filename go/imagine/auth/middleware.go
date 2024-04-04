package auth

import (
	"errors"
	"fmt"
	"net/http"
	"reflect"
	"strings"

	"github.com/cdfmlr/ellipsis"

	"github.com/gorilla/context"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

type Handler struct {
	enabled bool
	jwtAuth *JwtAuth
	account string
	logger  zerolog.Logger
}

type ContextKey string

// ContextAuthKey used to store auth info such as claims in request context
const ContextAuthKey ContextKey = "auth"

var authError = errors.New("auth error")

// New constructs a new Handler Context
func New(enabled bool, jwkUrl string, account string) *Handler {
	ctxLogger := log.With().Str("logger", "auth").Logger()
	ctxLogger.Info().Msgf("[AUTH] Init Handler enabled=%v jwks=%s account=%s", enabled, jwkUrl, account)
	jwtAuth, err := NewJwtAuth(jwkUrl)
	if err != nil {
		ctxLogger.Error().Msgf("[AUTH] JWKs from %s cannot be initialized, only own tokens will work: %v", jwkUrl, err)
	}
	return &Handler{
		jwtAuth: jwtAuth,
		enabled: enabled,
		account: account,
		logger:  ctxLogger,
	}
}

// CheckTokenMiddleware simple validation of static tokens
func (ah *Handler) CheckTokenMiddleware(expectedToken string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, req *http.Request) {
		authHeader := getAuthHeader(req)
		if strings.Contains(getAuthHeader(req), "Bearer") {
			hToken := strings.Fields(authHeader)[1]
			if hToken != expectedToken {
				handleError(w, fmt.Sprintf("authHeader token %s%s doesn't match expected token %s for path %s",
					ellipsis.Ending(hToken, 7), ellipsis.Starting(hToken, 4), ellipsis.Ending(expectedToken, 7), req.URL.Path),
					authError, http.StatusForbidden)
				return
			}
		} else {
			handleError(w, fmt.Sprintf("Invalid Auth Header %v", req.Header), authError, http.StatusForbidden)
			return
		}
		next(w, req)
	}
}

// ValidationMiddleware a wrapper around the actual request
// to validate the Authorization header and either stop processing (invalid / no expectedToken)
// or continue with the next HandlerFunc
// Make sure the client has the appropriate JWT if he/she wants to change things
// See also:
// https://hackernoon.com/creating-a-middleware-in-golang-for-jwt-based-authentication-cx3f32z8
func (ah *Handler) ValidationMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, req *http.Request) {
		// for now, we handle auth errors gracefully, but as soon as we enforce auth tokens those methods
		// won't pass the request further but return
		// Make sure the client has the appropriate JWT if he/she wants to change things
		// TODO Delegate to middleware, e.g. like this
		// https://hackernoon.com/creating-a-middleware-in-golang-for-jwt-based-authentication-cx3f32z8
		if ah.enabled {
			authHeader := getAuthHeader(req)
			if strings.Contains(authHeader, "Bearer") {
				jwtB64 := strings.Fields(authHeader)[1] // bearer part is first field
				claims, err := ah.jwtAuth.ParseClaims(authHeader)
				if err != nil {
					handleError(w, fmt.Sprintf("Failed to parse jwtb64 %v: %v", jwtB64, err), err, http.StatusForbidden)
					return
				}
				// scope is <nil> in case of "ordinary" User JWT
				// roles if present is =[arn:aws:iam::1245:role/angkor-cognito-role-user arn:aws:iam::12345:role/angkor-cognito-role-admin]
				// reflect.TypeOf(claims["cognito:roles"]) returns: array []interface {}
				if claims.Scope() == nil && claims.Roles() == nil {
					msg := "neither scope nor cognito:roles is present in JWT Claims"
					handleError(w, msg, errors.New(msg), http.StatusForbidden)
					return
				}
				ah.logger.Debug().Msgf("X-Authorization JWT Bearer Token claimsSub=%s scope=%v roles=%v name=%s roleType=%v",
					claims.Subject(), claims.Scope(), claims.Roles(), claims.Name(), reflect.TypeOf(claims.Roles()))
				context.Set(req, ContextAuthKey, claims)
			} else {
				handleError(w, fmt.Sprintf("Cannot find (X-)Authorization with Bearer Token in %v", req.Header), authError, http.StatusForbidden)
				return
			}
		} else {
			ah.logger.Warn().Msg("AuthSecurity is disabled, pass through mode")
		}
		next(w, req)
	}
}

// handleError logs the error and send http error response to client
func handleError(writer http.ResponseWriter, msg string, err error, code int) {
	log.Error().Msgf("Error %s - %v", msg, err)
	http.Error(writer, fmt.Sprintf("%s - %v", msg, err), code)
}

func getAuthHeader(req *http.Request) string {
	authHeader := req.Header.Get("X-Authorization") // case-insensitive
	if authHeader == "" {
		authHeader = req.Header.Get("Authorization") // fallback (e.g. for Grafana metrics scraping)
	}
	return authHeader
}
