#!/usr/bin/env bash
export EVENT_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
export TOPIC_ARN=arn:aws:sns:eu-central-1:$(cat ~/.angkor/.env|grep ACCOUNT_ID|cut -d= -f2-):angkor-events
export GITHUB_WORKFLOW=golang-ci
export GITHUB_RUN_ID=12345
export GITHUB_SHA=12345
export AWS_PROFILE=timafe
export SQS_POLLER_SLEEP_SECONDS=2

cat event.tmpl.json | envsubst >event.json
cat event.json
# https://docs.aws.amazon.com/cli/latest/reference/sns/publish.html
echo "Send event to $TOPIC_ARN"
aws sns publish --topic-arn $TOPIC_ARN \
    --message file://event.json \
    --message-attributes "GITHUB_SHA={DataType=String,StringValue=\"$GITHUB_SHA\"}, GITHUB_RUN_ID={DataType=String,StringValue=\"$GITHUB_RUN_ID\"}"

echo "Start sqs-poller locally, press ctrl-c to skip"
read dummy
go run main.go
