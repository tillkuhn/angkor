# Module specific Makefile for Frontend / UI / Angular Builds

# Make Special Variables https://www.gnu.org/software/make/manual/html_node/Special-Variables.html
# Sets the default goal to be used if no targets were specified on the command line
.DEFAULT_GOAL := help

# Custom Project Variables
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)

# Targets
.PHONY: help
help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: build
build: ## Run 'ng build' for dev purposes
	ng build

.PHONY: build-prod
build-prod: ## Run 'ng build --configuration production' to build prod distribution
	ng build --configuration production

.PHONY: clean
clean: ## Just wipe out the distribution folder ./dist
	rm -rf dist

.PHONY: lint
lint: ## Run ESLint tools in --fix mode, also launches hadolint for Dockerfile
	npx ng lint --fix angkor-ui
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile;

.PHONY: test
test: ## Runs jest tests (delegates to test target in package.json)
	yarn test

.PHONY: coverage # important since we also have a subfolder 'coverage'
coverage: ## Run 'yarn test:coverage' for jest tests with coverage, open report in browser
	yarn test:coverage
	open coverage/index.html

.PHONY: sonar
sonar: ## Run sonar-scanner locally, push results to sonarcloud.io
	../tools/sonar/node_modules/.bin/sonar-scanner \
	  -Dsonar.login=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
    -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn

.PHONY: mock
mock: ## run 'yarn mock' which launches json-server to mock api on port 8080
	yarn mock

.PHONY: mock-delay
mock-delay: ## Same as mock, but with artificial delay to test progress bars etc.
	yarn mock:delay

.PHONY: outdated
outdated: ## Run 'ng update' and 'yarn outdated' to show outdated dependencies
	@echo "Running ng update, see also https://update.angular.io/"
	ng version; ng update
	@echo "Let's also ask yarn about outdated packages, please updated manually with 'yarn upgrade <package>'"
	yarn outdated || echo "Outdated packages found"

.PHONY: update
ANGULAR_MAJOR_VERSION ?= 13
update: ## Update angular modules e.g. @angular/core and independent packages (most recent, same major)
	@echo "1) Updating Angular components within major version $(ANGULAR_MAJOR_VERSION)"
	ng update @angular/cli@^$(ANGULAR_MAJOR_VERSION) \
		@angular/core@^$(ANGULAR_MAJOR_VERSION) \
		@angular/cdk@^$(ANGULAR_MAJOR_VERSION) \
		@angular-eslint/schematics@^$(ANGULAR_MAJOR_VERSION) \
		@angular/material@^$(ANGULAR_MAJOR_VERSION) \
		@angular/youtube-player@^$(ANGULAR_MAJOR_VERSION)
	@echo "If in doubt for typescript etc. check https://github.com/angular/angular/blob/master/package.json"
	@echo "2) Updating other selected 3rd party packages (most recent same major)"
	yarn upgrade --latest --caret \
    @types/jest @types/mapbox-gl @types/node \
    eslint jest jest-preset-angular ts-jest mapbox-gl ngx-date-fns ngx-logger ngx-markdown zone.js
# Exlanation: --caret installs the most recent release with the same **major** version. Requires --latest
# Example: yarn upgrade mapbox-gl --latest --caret

.PHONY: run
run: ## Run 'ng serve --open' that runs http server on :4200 and opens a browser
	ng serve --open

.PHONY: scratch
scratch: ## Run typescript scratch.ts file for quick local tests
	@echo "Running ts-node scratch.ts"
	@ts-node scratch.ts || echo "ts-node failed, try 'yarn global add ts-node' if you have not installed it yet"

.PHONY: scra
scra: scratch ## Alias for scratch
