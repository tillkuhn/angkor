.ONESHELL:
.PHONY: format run lint
.DEFAULT_GOAL := help

BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
LDFLAGS_EXT ?= -extldflags '-static'
ARCH ?= arm64 #amd64
GOOS ?= linux

help:
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

format: ## format
	goimports -w .

lint: format
	# golangci-lint run
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

setup: ## mkdir -p dist
	echo "Default GOARCH=$(ARCH) LDFLAGS=$(LDFLAGS_BUILD_TIME) $(LDFLAGS_EXT)"
	mkdir -p dist

build-imagine: setup ## go build submodule
	# if you compile on linux for alpine, set CGO_ENABLED=0 g or you get "not found" when calling binary in container
	cd imagine;     go test . -v; env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/imagine ./*.go

build-healthbells: setup ## go build submodule
	cd healthbells; go test . -v; env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/healthbells ./*.go

build-polly: setup ## go build submodule
	cd polly; env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/polly ./*.go

build-remindabot: setup ## go build submodule
	cd remindabot; env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/remindabot ./*.go

build: build-imagine build-healthbells build-polly build-remindabot ## go build all submodules

docker: build ## docker buikd
	docker build -t angkor-tools .

dockerx: build ## docker cross platform build with buildx
	#  linux/amd64 linux/arm64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-tools .

clean: ## rm -rf dist
	rm -rf dist
