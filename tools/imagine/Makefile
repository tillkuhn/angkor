GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint

# As a call to `make` without any arguments leads to the execution
# of the first target found I really prefer to make sure that this
# first one is a non-destructive one that does the most simple
format:
	goimports -w .

run:
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
	 	AWS_PROFILE=timafe IMAGINE_TIMEOUT=17s  IMAGINE_CONTEXTPATH=/imagine go run *.go -file ~/tmp/elba.jpg; \
  	else echo "AWS_SESSION_TOKEN is present"; exit 1; fi

build:
	mkdir -p dist
    # GOOS=linux GOARCH=amd64 CGO_ENABLED=0
	go build -ldflags "-extldflags '-static'" -o dist/imagine ./*.go

build-linux:
	mkdir -p dist
	#  env trick, which let you set environment variables for that command only
	env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-extldflags '-static'" -o dist/linux/imagine ./*.go

lint: format
	golangci-lint run

clean:
	rm -rf dist

test:
	curl -Ss http://localhost:8090/imagine/places/110 |jq .
