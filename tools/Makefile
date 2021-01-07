.ONESHELL:
.PHONY: format run lint
.DEFAULT_GOAL := help

LDFLAGS ?= -extldflags '-static'
ARCH ?= arm64 #amd64

help:
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

format: ## format
	goimports -w .

lint: format
	# golangci-lint run
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile


setup: ## mkdir -p dist
	echo "Default GOARCH=$(ARCH) LDFLAGS=$(LDFLAGS)"
	mkdir -p dist

build-imagine: setup ## go build submodule
	# if you compile on linux for alpine, set CGO_ENABLED=0 g or you get "not found" when calling binary in container
	cd imagine; go test . -v; env GOOS=linux GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/imagine ./*.go

build-healthbells: setup ## go build submodule
	cd healthbells; go test . -v; env GOOS=linux GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/healthbells ./*.go

build-polly: setup ## go build submodule
	cd polly; env GOOS=linux GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "$(LDFLAGS)" -o ../dist/polly ./*.go

build: build-imagine build-healthbells build-polly ## go build all submodules

docker: build ## docker buikd
	docker build -t angkor-tools .

dockerx: build ## docker cross platform build with buildx
	#  linux/amd64 linux/arm64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-tools .

clean: ## rm -rf dist
	rm -rf dist
