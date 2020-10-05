GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: install  server scanner test release docs lint format

# As a call to `make` without any arguments leads to the execution
# of the first target found I really prefer to make sure that this
# first one is a non-destructive one that does the most simple
format:
	goimports -w .

run:
	AWS_PROFILE=timafe go run main.go

lint: format
	golangci-lint run

build:
	mkdir -p dist
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-extldflags '-static'" -o ./dist/app

