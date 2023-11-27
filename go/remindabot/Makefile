.ONESHELL:
.PHONY: help format fmt lint run dryrun dry
.DEFAULT_GOAL := help

GO_FILES := $(shell ls -1 *.go | grep -v _test.go) # only useful for run, not for build
CERTBOT_DOMAIN_NAME:=$(shell grep "^CERTBOT_DOMAIN_NAME" ~/.angkor/.env |cut -d= -f2-)
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)
REMINDABOT_API_TOKEN ?= $(shell grep "^REMINDABOT_API_TOKEN" ~/.angkor/.env |cut -d= -f2-)
# this is super ugly (share kafka dev credentials with api project), we need this in ~/.angkor/.env!
# see also imagine ...
KAFKA_SASL_USERNAME_DEV ?= $(shell grep sasl.jaas.config ../../kotlin/config/application.properties | cut -d\"  -f2)
KAFKA_SASL_PASSWORD_DEV ?= $(shell grep sasl.jaas.config ../../kotlin/config/application.properties | cut -d\"  -f4)

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## run goimports, write results to sourcefile (alias fmt)
	goimports -w .; go fmt .

fmt: format ## alias for format

lint: format ## format first, then golangci-lint run
	golangci-lint run


# https://github.com/psampaz/go-mod-outdated
outdated: ## show outdated direct dependencies
	go get -u github.com/psampaz/go-mod-outdated
	go list -u -m -json all | go-mod-outdated -direct

run: ## run main app
	env SOMEVAR=true go run $(GO_FILES)

runext: ## run  with external API Endpoint
	env REMINDABOT_API_URL=https://$(CERTBOT_DOMAIN_NAME)/api/v1/notes/reminders \
        go run $(GO_FILES)

test: ## run go tests
	go test

dryrun: ## dry run (alias dry)
	@# https://stackoverflow.com/a/45081909/4292075
	env KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) \
    	KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
    	KAFKA_TOPIC_PREFIX=$(KAFKA_SASL_USERNAME_DEV)- \
    	REMINDABOT_API_TOKEN=$(REMINDABOT_API_TOKEN) \
		REMINDABOT_SMTP_DRYRUN=true LOG_LEVEL=debug \
	go run $(GO_FILES)

dryext: ## dry run  with external API Endpoint
	env REMINDABOT_SMTP_DRYRUN=true \
    	REMINDABOT_API_TOKEN=$(REMINDABOT_API_TOKEN) \
 		REMINDABOT_API_URL=https://$(CERTBOT_DOMAIN_NAME)/api/v1/notes/reminders \
        go run $(GO_FILES)

dry403: ## dry run (alias dry) produces a 403
	env REMINDABOT_SMTP_DRYRUN=true REMINDABOT_API_TOKEN=nixexpect403 go run $(GO_FILES)

dry: dryrun

sonarcloud: ## run sonar-scanner to push to sonarcloud.io
	../../ui/node_modules/.bin/sonar-scanner \
	    -Dsonar.login=$(SONAR_TOKEN) \
    	-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.projectVersion=latest \
		-Dsonar.projectKey=remindabot \
		-Dsonar.organization=tillkuhn

update: ## go get -u all
	go get -u all
