package auth

import (
	"errors"
	"fmt"
	"net/http"
	"reflect"
	"strings"

	"github.com/gorilla/context"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

type HandlerContext struct {
	enabled bool
	jwtAuth *JwtAuth
	logger  zerolog.Logger
}

type ContextKey string

// ContextAuthKey used to store auth info such as claims in request context
const ContextAuthKey ContextKey = "auth"

// NewHandlerContext constructs a new AuthHandler Context
func NewHandlerContext(enabled bool, jwkUrl string) *HandlerContext {
	ctxLogger := log.With().Str("logger", "auth").Logger()
	ctxLogger.Info().Msgf("Init AuthHandler enabled=%v jwks=%s", enabled, jwkUrl)
	jwtAuth, err := NewJwtAuth(jwkUrl)
	if err != nil {
		ctxLogger.Error().Msgf("JWKs from %s cannot be initialized, only own tokens will work: %v", jwkUrl, err)
	}
	return &HandlerContext{
		jwtAuth: jwtAuth,
		enabled: enabled,
		logger:  ctxLogger,
	}
}

// AuthValidationMiddleware a wrapper around the actual request
// to validate the Authorization header and either stop processing (invalid / no token)
// or continue with the next HandlerFunc
// Make sure the client has the appropriate JWT if he/she wants to change things
// See also
// - https://hackernoon.com/creating-a-middleware-in-golang-for-jwt-based-authentication-cx3f32z8
func (ctx *HandlerContext) AuthValidationMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, req *http.Request) {
		// for now, we handle auth errors gracefully, but as soon as we enforce auth tokens those methods
		// won't pass the request further but return
		// Make sure the client has the appropriate JWT if he/she wants to change things
		// TODO Delegate to middleware, e.g. like this
		// https://hackernoon.com/creating-a-middleware-in-golang-for-jwt-based-authentication-cx3f32z8
		if ctx.enabled {
			authHeader := req.Header.Get("X-Authorization")
			if strings.Contains(authHeader, "Bearer") {
				jwtB64 := strings.Split(authHeader, "Bearer ")[1]
				claims, err := ctx.jwtAuth.ParseClaims(authHeader)
				if err != nil {
					handleError(w, fmt.Sprintf("Failed to parse jwtb64 %v: %v", jwtB64, err), err, http.StatusForbidden)
					return
				}
				// scope is <nil> in case of "ordinary" User JWT
				// roles if present is =[arn:aws:iam::1245:role/angkor-cognito-role-user arn:aws:iam::12345:role/angkor-cognito-role-admin]
				// reflect.TypeOf(claims["cognito:roles"]) is array []interface {}
				if claims.Scope() == nil && claims.Roles() == nil {
					msg := "neither scope nor cognito:roles is present in JWT Claims"
					handleError(w, msg, errors.New(msg), http.StatusForbidden)
					return
				}
				ctx.logger.Debug().Msgf("X-Authorization JWT Bearer Token claimsSub=%s scope=%v roles=%v name=%s roleType=%v",
					claims.Subject(), claims.Scope(), claims.Roles(), claims.Name(), reflect.TypeOf(claims.Roles()))
				context.Set(req, ContextAuthKey, claims)
			} else {
				handleError(w, fmt.Sprintf("Cannot find/validate X-Authorization header in %v", req.Header), errors.New("oops"), http.StatusForbidden)
				return
			}
		} else {
			ctx.logger.Warn().Msg("AuthSecurity is disabled, pass through mode")
		}
		next(w, req)
	}
}

// handleError logs the error and send http error response to client
func handleError(writer http.ResponseWriter, msg string, err error, code int) {
	log.Printf("[ERROR] %s - %v", msg, err)
	http.Error(writer, fmt.Sprintf("%s - %v", msg, err), code)
}
