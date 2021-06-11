#!/usr/bin/env bash
workdir=$(dirname "${BASH_SOURCE[0]}")
app=$(grep image "${workdir}"/docker-compose.yml | cut -d: -f2- | tr -d ' '|head -1)
echo "Starting $app"
mkdir -p "${workdir}"/sonarqube-data
docker-compose --file "${workdir}"/docker-compose.yml up --detach
echo "$app launched, you can now login as admin at http://localhost:9001/"
