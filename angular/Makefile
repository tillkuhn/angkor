# Module specific Makefile for Angular Builds
.DEFAULT_GOAL := help
.PHONY: help sonar

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)

help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

build: ## Run 'ng build' for dev purposes
	ng build

build-prod: ## Runs 'ng build --prod' to build production package
	ng build --prod

clean: ## Wipe out UI dist folder ./dist
	rm -rf dist

lint: ## Runs ESLint in --fix mode, also launches hadolint for Dockerfile
	npx ng lint --fix angkor-ui
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile;

mock: ## run yarn mock which launches json-server to mock api on port 8080
	yarn mock

mock-delay: ## same as mock, but with delay to test progress bars etc.
	yarn mock:delay

outdated: ## run 'ng update' and 'yarn outdated' to show outdated deps
	ng version; ng update
	echo "Let's also ask yarn about outdated packages"
	yarn outdated

update: ## update main angular modules such as @angular/core, cli, material etc.
	ng update @angular/cdk @angular/cli @angular/core @angular/material @angular/youtube-player

run: ## Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
	ng serve --open

scratch: ## run typescript scratch-file for quick local tests
	@echo "Running ts-node scratch.ts"
	@ts-node scratch.ts || echo "ts-node failed, try 'yarn global add ts-node' if you have not installed it yet"

scra: scratch ## alias for scratch

sonar: ## run sonar-scanner to push to sonarcloud.io
	../tools/sonar/node_modules/.bin/sonar-scanner \
		 -Dsonar.login=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
         -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn

test: ## Runs jest tests (delegates to test target in package.json)
	yarn test

.PHONY: coverage # important since we also have a subfolder 'coverage'
coverage: ## Runs jest tests with coverage and open report
	yarn test:coverage
	open coverage/index.html
