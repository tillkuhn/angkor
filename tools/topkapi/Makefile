.ONESHELL:
.PHONY: help format fmt lint run dryrun dry
.DEFAULT_GOAL := help

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)

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

run: ## run main app
	go run cmd/topkapi/main.go -topic default -action "makefile:test" -message "Let's run Kafka"

consume: ## run main app
	go run cmd/topkapi/main.go -topic default -consume

usage: ## run main app
	go run cmd/topkapi/main.go

sonarcloud: ## run sonar-scanner to push to sonarcloud.io
	../../ui/node_modules/.bin/sonar-scanner \
	    -Dsonar.login=$(SONAR_TOKEN) \
    	-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.projectVersion=latest \
		-Dsonar.projectKey=topkapi \
		-Dsonar.organization=tillkuhn


kowl: ## run kowl on localhost:8080 via docker-compose
	docker-compose up -d
	sleep 2 && open http://localhost:8080

kowl-down: ## shutdown kowl via docker-compose
	docker-compose down
