.ONESHELL:
.PHONY: help format fmt lint run dryrun dry .docker-check
.DEFAULT_GOAL := help

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)
KAFKA_BROKERS ?= $(shell grep "^app.kafka.brokers" ../../api/config/application.properties |cut -d= -f2-)
KAFKA_SASL_USERNAME_DEV ?= $(shell grep "^app.kafka.sasl-username" ../../api/config/application.properties |cut -d= -f2-)
KAFKA_SASL_PASSWORD_DEV ?= $(shell grep "^app.kafka.sasl-password" ../../api/config/application.properties |cut -d= -f2-)
KAFKA_SASL_USERNAME_PROD ?= $(shell grep "^KAFKA_SASL_USERNAME" ~/.angkor/.env | cut -d= -f2-)
KAFKA_SASL_PASSWORD_PROD ?= $(shell grep "^KAFKA_SASL_PASSWORD" ~/.angkor/.env |cut -d= -f2-)
# GO_FILES := $(shell ls -1 *.go | grep -v _test.go) # only useful for run, not for build

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## run goimports, write results to sourcefile (alias fmt)
	goimports -w .; go fmt ./...

fmt: format ## alias for format

lint: format ## format first, then golangci-lint run
	golangci-lint run

update: ## go get -u all
	go get -u all
	
test: ## run go tests
	go test

build: ## local native build
	go build -ldflags "-X 'main.BuildTime=$(shell date -u +"%Y-%m-%dT%H:%M:%SZ")' -X 'main.ReleaseName=local' -X 'main.AppVersion=latest' -extldflags '-static'" -o dist/topkapi cmd/topkapi/main.go

publish: ## publish a test message to dev topic
	env KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) \
		KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
		KAFKA_TOPIC_PREFIX=$(KAFKA_SASL_USERNAME_DEV)- \
	go run cmd/topkapi/main.go \
		-message "Let's have some $(shell curl -sS https://random-data-api.com/api/coffee/random_coffee | jq -r .blend_name) coffee" \
		-source "makefile" -topic audit -action "auth:test" -v

consume-all: ## run main app consume all dev topics
	env KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) \
		KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
		KAFKA_TOPIC_PREFIX=$(KAFKA_SASL_USERNAME_DEV)- \
	go run cmd/topkapi/main.go -topic app,system,audit,imagine -consume -timeout 8s

consume-audit: ## run main app consume app dev topic
	env KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) \
		KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
		KAFKA_TOPIC_PREFIX=$(KAFKA_SASL_USERNAME_DEV)- \
	go run cmd/topkapi/main.go -topic audit -consume -timeout 8s

usage: ## run main app
	go run cmd/topkapi/main.go

sonarcloud: ## run sonar-scanner to push to sonarcloud.io
	../../ui/node_modules/.bin/sonar-scanner \
	    -Dsonar.login=$(SONAR_TOKEN) \
    	-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.projectVersion=latest \
		-Dsonar.projectKey=topkapi \
		-Dsonar.organization=tillkuhn

# internal check if docker daemon is running
.docker-check:
	@if docker ps -q 2>/dev/null; then echo docker is running; else echo starting docker; open -a docker; sleep 16; fi

kowl-dev: .docker-check ## run kowl on localhost:8080 via docker-compose with DEV Credentials
	KAFKA_BROKERS=$(KAFKA_BROKERS) KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
    docker-compose -f docker-compose-kowl.yml  up -d
	sleep 2 && open http://localhost:9090

kowl: kowl-dev ## alias for kowl-dev

kowl-prod: .docker-check  ## run kowl on localhost:8080 via docker-compose with PROD Credentials
	KAFKA_BROKERS=$(KAFKA_BROKERS) KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_PROD) KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_PROD) \
    docker-compose -f docker-compose-kowl.yml  up -d
	sleep 2 && open http://localhost:9090

kowl-down: ## shutdown current kowl container via docker-compose
	docker-compose -f docker-compose-kowl.yml down
