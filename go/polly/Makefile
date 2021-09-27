GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint test

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format:
	goimports -w .

clean: ## rm -rf dist
	rm -rf dist
run: send ## run main.go
	AWS_PROFILE=timafe POLLY_QUEUE_NAME=angkor-events-dev POLLY_DELEGATE=test/appctl.sh POLLY_WAIT_SECONDS=1 POLLY_SLEEP_SECONDS=1 \
        go run $(shell ls -1 *.go | grep -v _test.go)

lint: format ## golangci-lint run
	golangci-lint run

# https://github.com/psampaz/go-mod-outdated
outdated: ## show outdated direct dependencies
	go get -u github.com/psampaz/go-mod-outdated
	go list -u -m -json all | go-mod-outdated -direct
	@echo "Run 'go get -u all' to update everything"

update: ## go get -u all
	go get -u all	

build: ## build for amd64
	mkdir -p dist
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-extldflags '-static'" -o ./dist/app

test: ## test worker
	go test ./worker

send: ## publish sns message to dev topic
	aws sns publish --topic-arn arn:aws:sns:eu-central-1:062960202541:angkor-events-dev --profile timafe  \
        --message "{\"action\":\"deploy-tools\",\"workflow\":\"workhard\"}"
