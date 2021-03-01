.DEFAULT_GOAL := help
.PHONY: help

SONAR_LOGIN ?= $(shell grep "^SONAR_LOGIN" ~/.angkor/.env |cut -d= -f2-)
SONAR_PASSWORD ?= $(shell grep "^SONAR_PASSWORD" ~/.angkor/.env |cut -d= -f2-)

help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

lint: ## A smarter Dockerfile linter that helps you build best practice Docker images
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile;

scratch: ## run typescript scratchfile
	ts-node scratch.ts

scra: scratch # alias

clean: ## Remove UI dist folder ./dist
	rm -rf dist

build: ## Run ng build
	ng build

build-prod: ## Run ng build --prod
	ng build --prod

test: ## Runs chromeHeadless tests
	ng test --browsers ChromeHeadless --watch=false --code-coverage

run: ## Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
	ng serve --open

mock: ## run yarn mock which launches json-server to mock api on port 8080
	yarn mock


mock-delay: ## same as mock, but with delay to test progress bars etc.
	yarn mock:delay

check-update: ## run ng-update
	ng version; ng update

sonarcloud: ## run sonar-scanner to push to sonarcloud.io
	./node_modules/.bin/sonar-scanner \
	  -Dsonar.login=$(SONAR_LOGIN) \
  	-Dsonar.host.url=https://sonarcloud.io \
		-Dsonar.projectVersion=latest \
		-Dsonar.projectKey=angkor \
		-Dsonar.organization=tillkuhn

	#-Dsonar.password=$(SONAR_PASSWORD)
