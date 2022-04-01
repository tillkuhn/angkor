.DEFAULT_GOAL := help
.PHONY: help clean test lint run build recreate-db get-places checkdb

API_LATEST = http://localhost:8080/api/v1
APP_API_TOKEN =$(shell grep "^APP_API_TOKEN" ~/.angkor/.env |cut -d= -f2-)

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)

help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

# Comon Targets for Gradle Projects and Docker Build

clean: ## Clean gradle build dir
	rm -rf build

coverage: ## Open jacoco coverage html report
	open build/reports/jacoco/test/html/index.html

lint: ## Lint Dockerfile with hadolint (requires local installation)
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile && echo "Dockerfile is considered OK"

build: ## Run gradle assemble
	gradle --build-cache --warning-mode all assemble

test: checkdb ## Launch unit and integration tests with gradle test
	gradle --build-cache --warning-mode all test --fail-fast --stacktrace

run: checkdb ## Launch with gradle bootRun
	gradle --build-cache bootRun

build-docker: build ## Run docker build for current platform, implies 'build'
	docker build -t angkor-api:latest .

buildx-docker: build ## Run docker cross platform linux/arm64  build(x), implies 'build'
	# platforms: linux/amd64 linux/arm64 darwin/amd64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-api:arm64 .

# For a list of available dependency configurations besides 'runtimeClasspath'
# @see https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_plugin_and_dependency_management
dependencies: ## Run gradle -q dependencies --configuration runtimeClasspath
	gradle -q dependencies --configuration runtimeClasspath

outdated: ## Check Project Dependency Updated using 'gradle dependencyUpdates'
	gradle dependencyUpdates
	@echo "Tip: Update centralized versions in gradle.properties"
	€echo "Tip: Check gradle updates with 'sdk list gradle' and also consider gradle/wrapper/gradle-wrapper.properties"

checkdb: ## Check if db is running, used as precondition by other tasks such as bootRun
	@pg_ctl -D /usr/local/var/postgres status; if [ $$? -eq 3 ]; then \
  		echo "Starting Postgres Server"; pg_ctl -D /usr/local/var/postgres start; \
  	else echo "Postgres Server already started"; fi

dbimport: ## Recreate local dev and test database
	./dbimport.sh

sonar-local: ## Run sonar-scanner with local sonar server
	gradle sonarqube \
		-Dsonar.projectKey=angkor-api \
        -Dsonar.projectName="Angkor API" \
        -Dsonar.host.url=http://localhost:9001/ \
	    -Dsonar.login=$(SONAR_LOGIN) \
    	-Dsonar.password=$(SONAR_PASSWORD)

sonar: ## Run sonar-scanner to push to sonarcloud.io
	gradle sonarqube -Dsonar.login=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
         -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn

# Integration tests #

get-areas: ## Fetch list of dishes using curl and jq
	curl -Ss $(API_LATEST)/areas/ | jq .

get-dishes: ## Fetch list of dishes using curl and jq
	curl -Ss $(API_LATEST)/dishes/search/ | jq .

get-feeds: ## Fetch list of videos (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/links/feeds | jq .

get-feed: ## Fetch list of videos (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/links/feeds/934a8504-7c61-4341-aa81-c16481c17feb | jq .

get-locations: ## Fetch list of locations with search patterns
	curl -Ss $(API_LATEST)/locations/search/eifel | jq .

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

get-tour: ## Get some external tour data
	curl -Ss $(API_LATEST)/tours/external/340398139 | jq .

get-tours: ## Get all tours (new location entity)
	curl -Ss $(API_LATEST)/tours/search/ | jq .

import-tour: ## Import tour using curl with ImportRequest Post
	curl -i -X POST --header "Content-Type: application/json" \
		-b JSESSIONID=$(shell cat cookie.txt) \
        --data '{"targetEntityType":"Tour","importUrl":"https://www.tumuult.de/tour/111...."}' \
        -Ss $(API_LATEST)/tours/import

import-video: ## Import video using curl with ImportRequest Post
	curl -i -X POST --header "Content-Type: application/json" \
		-b JSESSIONID=$(shell cat cookie.txt) \
        --data '{"targetEntityType":"Tour","importUrl":"https://youtu.be/NAUwEvZ_7Nc"}' \
        -Ss $(API_LATEST)/videos/import

get-videos: ## Fetch list of videos (with patterns etc.) with curl and jq
	curl -Ss $(API_LATEST)/links/videos | jq .

search-places: ## Search places using curl and jq with new SearchRequest Post
	curl -X POST --header "Content-Type: application/json" \
        --data '{"sortDirection":"DESC","sortProperties":["updatedAt"],"entityTypes":["Place"],"pageSize":3,"query":"bang"}' \
        -Ss $(API_LATEST)/locations/search | jq .

search-photos: ## Search photos using curl and jq with new SearchRequest Post
	curl -X POST --header "Content-Type: application/json" --data '{"entityTypes":["Photo"]}' \
        -Ss $(API_LATEST)/locations/search | jq .

search-locations: ## Search places using curl and jq with new SearchRequest Post
	curl -X POST --header "Content-Type: application/json" \
        --data '{"sortDirection":"DESC","sortProperties":["name"],"pageSize":3,"query":"island"}' \
        -Ss $(API_LATEST)/locations/search | jq .

search-any: ## Search locations based on entityType filter with new SearchRequest Post
	curl -X POST --header "Content-Type: application/json" \
        --data '{"sortDirection":"ASC","sortProperties":["name"],"entityTypes":["Place","Video","Post","Tour"],"pageSize":3,"query":"a"}' \
        -Ss $(API_LATEST)/locations/search | jq .

search-pois: ## Search pois starting with b based on locationSearch with minimal POI projection
	curl -X POST --header "Content-Type: application/json" \
        --data '{"sortDirection":"ASC","sortProperties":["name"],"pageSize":1,"query":"b"}' \
        -Ss $(API_LATEST)/locations/pois | jq .

webhook: ## Simulate post to github webhook endpoint
	curl --header "Content-Type: application/json" --header "X-Hub-Signature-256: 12345" --data '{"hase":"horst"}' -i http://localhost:8080/webhooks/github

