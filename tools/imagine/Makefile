GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint
TESTID := 4711

# As a call to `make` without any arguments leads to the execution
# of the first target found I really prefer to make sure that this
# first one is a non-destructive one that does the most simple
format:
	goimports -w .

run:
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
	 	AWS_PROFILE=timafe IMAGINE_RESIZE_QUALITY=79 IMAGINE_S3BUCKET=timafe-angkor-data-dev \
	 	IMAGINE_ENABLE_AUTH=false \
	 	IMAGINE_RESIZE_MODES=small:150,medium:300,large:600 IMAGINE_CONTEXTPATH=/imagine \
		 go run main.go http.go image.go model.go s3worker.go; \
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
	go test

testget:
	curl -Ss http://localhost:8090/imagine/places/$(TESTID) |jq .

testpost:
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://img.br.de/d01e016b-2361-4f74-b803-aca4e074d87f.jpeg","filename": "hase2"}' http://localhost:8090/imagine/places/$(TESTID)
