GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
IMAGINE_JWKS_ENDPOINT=$(shell grep "^IMAGINE_JWKS_ENDPOINT" ~/.angkor/.env |cut -d= -f2-)
JWT_TOKEN :=$(shell ../../tools/jwt.sh |jq --raw-output  .access_token)
.ONESHELL:
.PHONY: format fmt lint run build build-linux clean test
GO_FILES := $(shell ls -1 *.go | grep -v _test.go)
TEST_ID := 4711
.DEFAULT_GOAL := help

KAFKA_SASL_USERNAME_DEV ?= $(shell grep "^app.kafka.sasl-username" ../../kotlin/config/application.properties |cut -d= -f2-)
KAFKA_SASL_PASSWORD_DEV ?= $(shell grep "^app.kafka.sasl-password" ../../kotlin/config/application.properties |cut -d= -f2-)

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

upgrade: ## runs go get -u all
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

test:  ## run go tests
	go test . ./auth/

testget: ## curl files from test place
	curl -Ss http://localhost:8090/imagine/places/$(TEST_ID) |jq .

testhealth: ## test healthcheck
	curl -Ss http://localhost:8090/imagine/health |jq .

testpost: ## test post image with filename
	curl -i -X POST --header "Content-Type: application/json" \
		--header "X-Authorization: Bearer $(JWT_TOKEN)" \
 		--data '{"url":"https://img.br.de/d01e016b-2361-4f74-b803-aca4e074d87f.jpeg","filename": "hase2"}' http://localhost:8090/imagine/places/$(TEST_ID)

testpost-noext: ## test post image with no filename extension
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://asiastreetfood.com/wp-content/uploads/2014/07/Laab-Rezept-Ente-Laos"}' http://localhost:8090/imagine/places/$(TEST_ID)

testpost-noext2: ## test post image request params in url.,
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://www.kuechengoetter.de/uploads/media/960x960/00/73130-kritharaki-in-gemuese-tomaten-sauce.jpg?v=1-0"}' http://localhost:8090/imagine/places/$(TEST_ID)

testpost-encoded: ## test post image url request with encoded url.,
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://paradise-found.de/wp-content/uploads/2019/10/Gotland-Sehensw%C3%BCrdigkeiten-und-Tipps-f%C3%BCr-Visby-78.jpg"}' http://localhost:8090/imagine/places/$(TEST_ID)

