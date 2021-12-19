#!/usr/bin/env bash
# Exchanging Client Credentials for an Access Token (JWT)
#
# https://lobster1234.github.io/2018/05/31/server-to-server-auth-with-amazon-cognito/
# https://docs.aws.amazon.com/cognito/latest/developerguide/token-endpoint.html
# https://aws-blog.de/2020/01/machine-to-machine-authentication-with-cognito-and-serverless.html
# https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-resource-servers.html
#scope={resourceServerIdentifier1}/{scope1} {resourceServerIdentifier2}/{scope2}

extract=false

while getopts ":hx" option; do
  case $option in
    h) echo "usage: $0 [-h] [-x]"; exit ;;
    x) extract=true ;;
    ?) echo "error: option -$OPTARG is not implemented"; exit ;;
  esac
done

token_endpoint=$(grep OAUTH2_TOKEN_ENDPOINT ~/.angkor/.env|cut -d= -f2-)
client_id=$(grep OAUTH2_CLIENT_CLI_ID ~/.angkor/.env|cut -d= -f2-)
client_secret=$(grep OAUTH2_CLIENT_CLI_SECRET ~/.angkor/.env|cut -d= -f2-)
authorization=$(echo -n "${client_id}:${client_secret}" | base64)
scope_prefix="angkor-resources"

# dump into temporary file so we can conditionally extract from it, make sure it's deleted on exit
temp_file=$(mktemp)
trap '{ rm -f -- "$temp_file"; }' EXIT

curl -sS -X POST $token_endpoint \
  -H "Authorization: Basic $authorization" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=${scope_prefix}%2Fread ${scope_prefix}%2Fwrite" >"$temp_file"

if ! grep -q "access_token" "$temp_file"; then
  echo "Error in response"
  cat "$temp_file"
  echo
  exit 1
fi

if [ $extract == "true" ]; then
  jq -r '.access_token' "$temp_file"
else
  cat "$temp_file"
  echo
fi

