.ONESHELL:
.PHONY: format run lint

LDFLAGS ?= -extldflags '-static'

# As a call to `make` without any arguments leads to the execution
# of the first target found I really prefer to make sure that this
# first one is a non-destructive one that does the most simple
format:
	goimports -w .

setup:
	mkdir -p dist

build-imagine: setup
	# if you compile on linux for alpine, set CGO_ENABLED=0 g or you get "not found" when calling binary in container
	cd imagine; go test . -v; env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/imagine ./*.go

build-healthbells: setup
	cd healthbells; env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/healthbells ./*.go

build-polly: setup
	cd polly; env GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/polly ./*.go

build: build-imagine build-healthbells build-polly

docker: build
	docker build -t angkor-tools .

lint: format
	golangci-lint run

clean:
	rm -rf dist
