https://github.com/appvia/mock-oidc-user-server

mock-oidc-user-server
Build_Status Docker Repository on Quay

WARNING: DO NOT USE IN PRODUCTION
A mock user server providing OpenID Connect (OIDC) flows for development and testing.

Uses the excellent node-oidc-provider, which comes with dev interactions/flows out of the box (OIDC compliant). Any username and password combination is permitted for logging in, making this very useful for development and CI.

20:13 unfall 

Sample URL:
http://localhost:9090/auth?response_type=code&client_id=my-client&scope=openid&redirect_uri=http://localhost:8080/cb

Real Cognito endpoint:
https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_<someid>/.well-known/openid-configuration

config
```
- CLIENT_ID=my-client
- CLIENT_SECRET=my-secret
- CLIENT_REDIRECT_URI=http://localhost:8080/xxx
- CLIENT_LOGOUT_REDIRECT_URI=http://localhost:8080
```

server log
```
WARNING: a quick start development-only sign key is used, you are expected to provide your own during provider#initialize
WARNING: a quick start development-only MemoryAdapter is used
WARNING: a quick start development-only feature devInteractions is enabled, you are expected to disable these interactions and provide your own
mock-oidc-user-server listening on port 9090, check http://localhost:9090/.well-known/openid-configuration
```
