#!/usr/bin/env bash
# Exchanging Client Credentials for an Access Token
#
# https://lobster1234.github.io/2018/05/31/server-to-server-auth-with-amazon-cognito/
# https://docs.aws.amazon.com/cognito/latest/developerguide/token-endpoint.html
# https://aws-blog.de/2020/01/machine-to-machine-authentication-with-cognito-and-serverless.html
# https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-resource-servers.html
# 
token_endpoint=$(grep OAUTH2_TOKEN_ENDPOINT ~/.angkor/.env|cut -d= -f2-)
client_id=$(grep OAUTH2_CLIENT_CLI_ID ~/.angkor/.env|cut -d= -f2-)
client_secret=$(grep OAUTH2_CLIENT_CLI_SECRET ~/.angkor/.env|cut -d= -f2-)
authorization=$(echo -n "${client_id}:${client_secret}" | base64)
scope_prefix="angkor-resources"

curl -s -X POST $token_endpoint \
  -H "Authorization: Basic $authorization" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=${scope_prefix}%2Fread ${scope_prefix}%2Fwrite"

#scope={resourceServerIdentifier1}/{scope1} {resourceServerIdentifier2}/{scope2}
