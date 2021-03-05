.ONESHELL:
.PHONY: format run lint sonar
.DEFAULT_GOAL := help

LDFLAGS_EXT ?= -extldflags '-static'
BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
# default for local execution, run as SONAR_TOKEN=xxx in CI where we pull them from github secrets or AWS SSM
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
RELEASE_NAME ?= makefile-release # should be passed in through environment, e.g. by github workflow

# for cross platform builds, e.g. use GOOS=darwin ARCH=amd64 make build
ARCH ?= arm64 # only set if not present, e.g. vars already set in env have precedence
GOOS ?= linux # https://gist.github.com/asukakenji/f15ba7e588ac42795f421b48b8aede63#a-list-of-valid-goos-values

help:
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

format: ## goimports -w .
	goimports -w .

lint: format ## goimports -w . + hadolint Dockerfile
	# golangci-lint run
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

setup: ## mkdir -p dist
	echo "Default GOARCH=$(ARCH) LDFLAGS=$(LDFLAGS_BUILD_TIME) $(LDFLAGS_EXT)"
	mkdir -p dist

test: ## run only tests in all subprojects
	for T in imagine healthbells remindabot; do cd $$T; go test . -v; cd ..; done
	cd polly; go test ./worker -v;

build-imagine: setup ## go build submodule
	# if you compile on linux for alpine, set CGO_ENABLED=0 g or you get "not found" when calling binary in container
	cd imagine; go test . -coverprofile="coverage.out" -v;\
 	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/imagine ./*.go

build-healthbells: setup ## go build submodule
	cd healthbells; go test . -coverprofile="coverage.out" -v;\
 	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/healthbells ./*.go

build-polly: setup ## go build submodule
	cd polly; go test ./worker -coverprofile="coverage.out"  -v;\
 	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/polly ./*.go

build-remindabot: setup ## go build submodule
	cd remindabot; go test . -coverprofile="coverage.out" -v; \
	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' -X 'main.ReleaseName=$(RELEASE_NAME)' -X 'main.AppVersion=$(RELEASE_VERSION)' $(LDFLAGS_EXT)" -o ../dist/remindabot ./*.go

build: build-imagine build-healthbells build-polly build-remindabot ## go build all submodules

docker: build ## build docker image
	docker build -t angkor-tools .

dockerx: build ## docker cross platform build with buildx
	#  linux/amd64 linux/arm64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-tools .

clean: ## rm -rf dist
	rm -rf dist

sonar: ## run sonarqube-scanner for all supported tools
	SONAR_TOKEN=$(SONAR_TOKEN) RELEASE_VERSION=$(RELEASE_VERSION) ./sonar-scanner.sh
