GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format:
	goimports -w .

clean: ## rm -rf dist
	rm -rf dist
run: send ## run main.go
	AWS_PROFILE=timafe POLLY_QUEUE_NAME=angkor-events-dev POLLY_DELEGATE=test/appctl.sh go run main.go

lint: format ## golangci-lint run
	golangci-lint run

build: ## build for amd64
	mkdir -p dist
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-extldflags '-static'" -o ./dist/app

test: ## test worker
	go test ./worker

send: ## publish sns message to dev topic
	aws sns publish --topic-arn arn:aws:sns:eu-central-1:062960202541:angkor-events-dev --profile timafe  \
        --message "{\"action\":\"test\",\"workflow\":\"workhard\"}"
