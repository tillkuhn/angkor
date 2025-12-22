.ONESHELL:
.PHONY: format run lint sonar test deploy-remindabot
.DEFAULT_GOAL := help
ENV_FILE ?= ~/.angkor/.env
SSH_OPTIONS ?= -o StrictHostKeyChecking=no

LDFLAGS_EXT ?= -extldflags '-static'
BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")

# default for local execution, run as SONAR_TOKEN=xxx in CI where we pull them from github secrets or AWS SSM
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
RELEASE_NAME ?= makefile-release # should be passed in through environment, e.g. by github workflow
GO_PROJECTS ?= healthbells imagine remindabot

# for cross platform builds, e.g. use GOOS=darwin ARCH=amd64 make build
ARCH ?= arm64 # only set if not present, e.g. vars already set in env have precedence
GOOS ?= linux # https://gist.github.com/asukakenji/f15ba7e588ac42795f421b48b8aede63#a-list-of-valid-goos-values

help: ## this help
	@# https://gist.github.com/prwhite/8168133#gistcomment-3291344
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

setup: ## mkdir -p dist
	@echo "Default GOARCH=$(ARCH) LDFLAGS=$(LDFLAGS_BUILD_TIME) $(LDFLAGS_EXT)"
	mkdir -p dist

clean: ## cleanup 'rm -rf dist'
	rm -rf dist

test: ## run only tests in all subprojects
	$(MAKE) -C ./imagine test
	for T in healthbells remindabot; do cd $$T; go test . -v; cd ..; done

build-imagine: setup ## go build submodule imagine
	cd imagine; go test ./... -coverprofile="coverage.out"  -v;\
 	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/imagine ./*.go

build-healthbells: setup ## go build submodule healthbells
	cd healthbells; go test . -coverprofile="coverage.out" -v;\
 	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' $(LDFLAGS_EXT)" -o ../dist/healthbells ./*.go


build-remindabot: setup ## go build submodule remindabot
	cd remindabot; go test . -coverprofile="coverage.out" -v; \
	env GOOS=$(GOOS) GOARCH=$(ARCH) CGO_ENABLED=0 go build -ldflags "-X 'main.BuildTime=$(BUILD_TIME)' -X 'main.ReleaseName=$(RELEASE_NAME)' -X 'main.AppVersion=$(RELEASE_VERSION)' $(LDFLAGS_EXT)" -o ../dist/remindabot ./*.go

## go build all submodules and libraries
.PHONY: build
build: build-imagine build-healthbells build-remindabot

# make sure remote .bashrc does not output anything in none-interactive mode, or scp fails see https://serverfault.com/a/328404
deploy-remindabot: build-remindabot ## shortcut deploy to remote for hotfixes
	scp -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-) $(SSH_OPTIONS)  ./dist/remindabot ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-):/home/ec2-user/tools/remindabot


docker: build ## build docker image
	docker build -t angkor-tools .

dockerx: build ## docker cross platform build for linux/arm64 with buildx
	@#  linux/amd64 ("classic" AMD) vs linux/arm64 (RISC Architecture)
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-tools .

sonar: ## run sonarqube-scanner for all supported tools
	SONAR_TOKEN=$(SONAR_TOKEN) RELEASE_VERSION=$(RELEASE_VERSION) ../tools/sonar-scanner.sh

update: ## update dependencies on all go projects
	@# https://stackoverflow.com/a/15679887/4292075 Command to get latest Git commit hash from a branch
	@# https://stackoverflow.com/a/53682399/4292075 How to point Go module dependency in go.mod to a latest commit in a repo?
	@for T in $(GO_PROJECTS); do \
		cd $$T; \
		echo "Updating $$T"; \
		go get -u; \
		cd ..; \
	done

tidy: ## runs 'go mod tidy' on all Go projects
	@for T in $(GO_PROJECTS); do \
		cd $$T; echo "Tidy up $$T"; go mod tidy ; cd ..; \
	done

fmt: tidy ## runs 'go fmt' on all Go projects, implies tidy (todo: also run 'goimports -w')
	@for T in $(GO_PROJECTS); do \
		cd $$T; echo "Format $$T"; go fmt . ; cd ..; \
	done
