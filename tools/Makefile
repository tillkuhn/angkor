.ONESHELL:
.DEFAULT_GOAL := help
ENV_FILE ?= ~/.angkor/.env
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
RELEASE_NAME ?= makefile-release # should be passed in through environment, e.g. by github workflow
BREW_TOOLS :=  awscli git go  golangci-lint hadolint jq postgresql@12 tfswitch yarn

.PHONY: help
help:
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

.PHONY: sonar-scanner
sonar-scanner: ## run sonarqube-scanner for all supported tools
	SONAR_TOKEN=$(SONAR_TOKEN) RELEASE_VERSION=$(RELEASE_VERSION) ./sonar-scanner.sh

.PHONY: sonar
sonar: ## run local sonar in docker
	docker-compose --file "$(CURDIR)"/sonar/docker-compose.yml up --detach

.PHONY: kowlma
kowl: ## run kowl on localhost:8080 via docker-compose
	docker-compose -f $(CURDIR)/../go/topkapi/docker-compose-kowl.yml up

.PHONY: install
install: ## install external tools that make our life easier	  
	for brewtool in $(BREW_TOOLS); do brew install $$brewtool; done

.PHONY: upgrade
upgrade: ## upgrade external tools that make our life easier	  
	for brewtool in $(BREW_TOOLS); do brew upgrade $$brewtool; done
	@echo "Finished for our core tools managed by brew, to get a full outdated list run 'brew outdated'"

