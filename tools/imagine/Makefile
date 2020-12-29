GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint
TESTID := 4711

.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## format sourcefiles
	goimports -w .

run: ## run app
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
	 	AWS_PROFILE=timafe IMAGINE_RESIZE_QUALITY=79 IMAGINE_S3BUCKET=timafe-angkor-data-dev \
	 	IMAGINE_FORCE_GC=true \
	 	IMAGINE_ENABLE_AUTH=false \
	 	IMAGINE_RESIZE_MODES=small:150,medium:300,large:600 IMAGINE_CONTEXTPATH=/imagine \
		 go run main.go http.go image.go model.go s3worker.go util.go; \
  	else echo "AWS_SESSION_TOKEN is present"; exit 1; fi

build: ## build to dist
	mkdir -p dist
    # GOOS=linux GOARCH=amd64 CGO_ENABLED=0
	go build -ldflags "-extldflags '-static'" -o dist/imagine ./*.go

build-linux:
	mkdir -p dist
	#  env trick, which let you set environment variables for that command only
	env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "-extldflags '-static'" -o dist/linux/imagine ./*.go

lint: format
	golangci-lint run

clean: ## rm dist
	rm -rf dist

test:  ## run go tests
	go test

testget: ## curl files from test place
	curl -Ss http://localhost:8090/imagine/places/$(TESTID) |jq .

testhealth: ## test healthcheck
	curl -Ss http://localhost:8090/imagine/health |jq .

testpost: ## test post image with filename
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://img.br.de/d01e016b-2361-4f74-b803-aca4e074d87f.jpeg","filename": "hase2"}' http://localhost:8090/imagine/places/$(TESTID)

testpost-noext: ## test post image with no filename extension
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://asiastreetfood.com/wp-content/uploads/2014/07/Laab-Rezept-Ente-Laos"}' http://localhost:8090/imagine/places/$(TESTID)

testpost-noext2: ## test post image request params in url.,
	curl -i -X POST --header "Content-Type: application/json" \
 		--data '{"url":"https://www.kuechengoetter.de/uploads/media/960x960/00/73130-kritharaki-in-gemuese-tomaten-sauce.jpg?v=1-0"}' http://localhost:8090/imagine/places/$(TESTID)
