.DEFAULT_GOAL := help
.PHONY: help clean test lint run build recreate-db get-places checkdb
API_LATEST = http://localhost:8080/api/v1
APP_API_TOKEN =$(shell grep "^APP_API_TOKEN" ~/.angkor/.env |cut -d= -f2-)

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)

help: ## this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

clean: ## clean build dir
	rm -rf build

coverage: ## open jacoco coverage html report
	open build/reports/jacoco/test/html/index.html

test: ## run test
	gradle test --fail-fast --stacktrace;

dependencies: ## gradle dependencyUpdates
	gradle dependencyUpdates

lint: ## A smarter Dockerfile linter that helps you build best practice Docker images
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

checkdb: ## check if db is running
	@pg_ctl -D /usr/local/var/postgres status; if [ $$? -eq 3 ]; then \
  		echo "Starting PostgreSQL"; pg_ctl -D /usr/local/var/postgres start; \
  	else echo "PostgreSQL already started"; fi

run: checkdb ## gradle bootRun
	gradle bootRun

build: ## gradle assemble
	gradle assemble

dockerx: build ## docker cross platform build with buildx
	#  linux/amd64 linux/arm64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-api .

recreate-db: ## Recreate local dev and test DB
	./recreate-db.sh

importdb: recreate-db ## Alias for recreate-db

get-dishes: ## Fetch list of dishes using curl and jq
	curl -Ss $(API_LATEST)/dishes/search/ | jq .

get-notes: ## Fetch list of dishes using curl and jq
	curl -Ss $(API_LATEST)/notes/search/ | jq .

get-note-tags: ## Fetch list of tags  applicable for notes using curl and jq
	curl -Ss $(API_LATEST)/tags/Note | jq .

get-places: ## Fetch list of places using curl and jq
	curl -Ss $(API_LATEST)/places/search/ | jq .

get-place-tags: ## Fetch list of tags applicable for places using curl and jq
	curl -Ss $(API_LATEST)/tags/PLACE | jq .

get-pois: ## Fetch list of places using curl and jq
	curl -Ss $(API_LATEST)/pois/ | jq .

get-reminders: ## Fetch list of reminders using curl, auth token and jq
	curl -H "X-Auth-Token: $(APP_API_TOKEN)" -Ss $(API_LATEST)/notes/reminders/ | jq .

get-tags: ## Fetch list of tags (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/tags | jq .

get-stats: ## Fetch list of tags (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/stats | jq .

get-videos: ## Fetch list of videos (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/videos | jq .

search-places: ## Search places using curl and jq with new SearchRequest Post
	curl -X POST --header "Content-Type: application/json" \
        --data '{"sortDirection":"DESC","sortProperties":["locationType"],"pageSize":3,"query":"bang"}' \
        -Ss $(API_LATEST)/places/search | jq .

sonar-local: ## run sonar-scanner
	gradle sonarqube \
		-Dsonar.projectKey=angkor-api \
        -Dsonar.projectName="Angkor API" \
        -Dsonar.host.url=http://localhost:9001/ \
	    -Dsonar.login=$(SONAR_LOGIN) \
    	-Dsonar.password=$(SONAR_PASSWORD)

sonar: ## run sonar-scanner to push to sonarcloud.io
	gradle sonarqube -Dsonar.login=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
         -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn
