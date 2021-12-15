TEST_ID := 4711
GO_FILES := $(shell ls -1 *.go | grep -v _test.go)
GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
JWT_TOKEN :=$(shell ../../tools/jwt.sh |jq --raw-output  .access_token)
# this is super ugly (share kafka dev credentials with api project), we need this in ~/.angkor/.env!
KAFKA_SASL_USERNAME_DEV ?= $(shell grep sasl.jaas.config ../../kotlin/config/application.properties | cut -d\"  -f2)
KAFKA_SASL_PASSWORD_DEV ?= $(shell grep sasl.jaas.config ../../kotlin/config/application.properties | cut -d\"  -f4)
IMAGINE_JWKS_ENDPOINT=$(shell grep "^IMAGINE_JWKS_ENDPOINT" ~/.angkor/.env |cut -d= -f2-)

.ONESHELL:
.PHONY: format fmt lint run build build-linux clean test
.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## format sources
	go fmt .
	goimports -w .

fmt: format ## alias for format

# https://github.com/psampaz/go-mod-outdated
outdated: ## show outdated direct dependencies
	go install github.com/psampaz/go-mod-outdated@latest
	go list -u -m -json all | go-mod-outdated -direct
	@echo "Run 'go get -u all' or 'make upgrade' to update everything"

update: ## update dependencies by running 'go get -u all'
	go get -u all

lint: format
	golangci-lint run

run: ## run app
	@if [ 200 -eq $(shell curl --write-out '%{http_code}' --silent --output /dev/null http://localhost:8090/imagine/health) ];  then \
		echo "IMAGINE is already running on port 8090"; exit 200; \
	fi
	@if [ -z $(AWS_SESSION_TOKEN) ]; then \
		env KAFKA_SASL_USERNAME=$(KAFKA_SASL_USERNAME_DEV) \
		KAFKA_SASL_PASSWORD=$(KAFKA_SASL_PASSWORD_DEV) \
		KAFKA_TOPIC_PREFIX=$(KAFKA_SASL_USERNAME_DEV)- \
	 	AWS_PROFILE=timafe \
	 	IMAGINE_RESIZE_QUALITY=79 \
	 	IMAGINE_S3BUCKET=timafe-angkor-data-dev \
	 	IMAGINE_FORCE_GC=true \
	 	IMAGINE_ENABLE_AUTH=true \
	 	IMAGINE_JWKS_ENDPOINT=$(IMAGINE_JWKS_ENDPOINT) \
	 	IMAGINE_RESIZE_MODES=small:150,medium:300,large:600 IMAGINE_CONTEXTPATH=/imagine \
		 go run $(GO_FILES); \
  	else echo "AWS_SESSION_TOKEN is present, pls open a fresh terminal"; exit 1; fi

build: ## build to dist current OS
	mkdir -p dist
    # GOOS=linux GOARCH=amd64 CGO_ENABLED=0
	go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' -extldflags '-static'" -o dist/imagine ./*.go

build-linux: ## build to dist linux OS
	mkdir -p dist
	#  env trick, which let you set environment variables for that command only
	# https://polyverse.com/blog/how-to-embed-versioning-information-in-go-applications-f76e2579b572/
	env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' -extldflags '-static'" -o dist/linux/imagine ./*.go

clean: ## rm dist
	rm -rf dist

test:  ## run go tests (with colorized output support)
	go install github.com/rakyll/gotest@latest 
	gotest -v . ./auth/ ./audio/

test-health: ## test health check
	curl -Ss http://localhost:8090/imagine/health |jq .

test-get: ## test get files for id
	curl -Ss http://localhost:8090/imagine/places/$(TEST_ID) |jq .

test-song: ## test upload of song file using multipart/form-data
	curl -i -X POST -H "Content-Type: multipart/form-data" \
		-H "X-Authorization: Bearer $(JWT_TOKEN)" \
	  	-F "uploadfile=@static/test-song.mp3"  \
 		http://localhost:8090/imagine/songs

test-post: ## test post image with filename
	curl -i -X POST --header "Content-Type: application/json" \
		--header "X-Authorization: Bearer $(JWT_TOKEN)" \
 		--data '{"url":"https://img.br.de/d01e016b-2361-4f74-b803-aca4e074d87f.jpeg","filename": "hase2"}' \
 		http://localhost:8090/imagine/places/$(TEST_ID)

test-tour: ## test post image with filename to tours endpoint
	curl -i -X POST --header "Content-Type: application/json" \
		--header "X-Authorization: Bearer $(JWT_TOKEN)" \
 		--data '{"url":"https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Eiffelturm.JPG/180px-Eiffelturm.JPG","filename": "tour-eiffel"}' \
 		http://localhost:8090/imagine/tours/$(TEST_ID)

test-post-noext: ## test post image with no filename extension in url
	curl -i -X POST --header "Content-Type: application/json" \
		--header "X-Authorization: Bearer $(JWT_TOKEN)" \
 		--data '{"url":"https://asiastreetfood.com/wp-content/uploads/2014/07/Laab-Rezept-Ente-Laos"}' \
 		http://localhost:8090/imagine/places/$(TEST_ID)

test-post-gpx: ## test post image with no filename extension in url
	curl -i -X POST --header "Content-Type: application/json" \
		--header "X-Authorization: Bearer $(JWT_TOKEN)" \
 		--data '{"url":"https://www.komoot.de/api/v007/tours/553371366.gpx","filename": "tour-data.gpx"}' \
 		http://localhost:8090/imagine/tours/$(TEST_ID)

test-post-noext2: ## test post image request params in url.,
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://www.kuechengoetter.de/uploads/media/960x960/00/73130-kritharaki-in-gemuese-tomaten-sauce.jpg?v=1-0"}'\
 		http://localhost:8090/imagine/places/$(TEST_ID)

test-post-encoded: ## test post image with url-encoded url
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://paradise-found.de/wp-content/uploads/2019/10/Gotland-Sehensw%C3%BCrdigkeiten-und-Tipps-f%C3%BCr-Visby-78.jpg"}' \
 		http://localhost:8090/imagine/places/$(TEST_ID)

