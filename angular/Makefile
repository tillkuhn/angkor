# Module specific Makefile for Angular Builds
.DEFAULT_GOAL := help

RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)

.PHONY: help
help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: build
build: ## Run 'ng build' for dev purposes
	ng build

.PHONY: build-prod
build-prod: ## Runs 'ng build --prod' to build production package
	ng build --prod

.PHONY: clean
clean: ## Wipe out UI dist folder ./dist
	rm -rf dist

.PHONY: lint
lint: ## Runs ESLint in --fix mode, also launches hadolint for Dockerfile
	npx ng lint --fix angkor-ui
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile;

.PHONY: mock
mock: ## run yarn mock which launches json-server to mock api on port 8080
	yarn mock

.PHONY: mock-delay
mock-delay: ## same as mock, but with delay to test progress bars etc.
	yarn mock:delay

.PHONY: outdated
outdated: ## run 'ng update' and 'yarn outdated' to show outdated dependencies
	ng version; ng update
	@echo "Let's also ask yarn about outdated packages, please updated manually with 'yarn upgrade <package>'"
	yarn outdated || echo "Outdated packages found"

.PHONY: update
ANGULAR_MAJOR_VERSION ?= 13
update: ## update angular modules e.g. @angular/core and other independent packages (most recent same major)
	@echo "1) Updating Angular components within major version $(ANGULAR_MAJOR_VERSION)"
	ng update @angular/cli@^$(ANGULAR_MAJOR_VERSION) \
		@angular/core@^$(ANGULAR_MAJOR_VERSION) \
		@angular/cdk@^$(ANGULAR_MAJOR_VERSION) \
		@angular-eslint/schematics@^$(ANGULAR_MAJOR_VERSION) \
		@angular/material@^$(ANGULAR_MAJOR_VERSION) \
		@angular/youtube-player@^$(ANGULAR_MAJOR_VERSION)
	@echo "1) Updating other selected 3rd party packages (most recent same major)"
	yarn upgrade --latest --caret \
    @types/jest @types/mapbox-gl @types/node \
    eslint jest jest-preset-angular mapbox-gl ngx-date-fns ngx-markdown
# todo could add additional modules
# --caret: install most recent release with the same **major** version. Only used when --latest is specified
# yarn upgrade mapbox-gl --latest --caret
# for The CPU architecture "x64" is incompatible with this module. warnings see
# https://github.com/swc-project/swc/issues/2374

.PHONY: run
run: ## Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
	ng serve --open

.PHONY: scratch
scratch: ## run typescript scratch-file for quick local tests
	@echo "Running ts-node scratch.ts"
	@ts-node scratch.ts || echo "ts-node failed, try 'yarn global add ts-node' if you have not installed it yet"

.PHONY: scra
scra: scratch ## alias for scratch

.PHONY: sonar
sonar: ## run sonar-scanner to push to sonarcloud.io
	../tools/sonar/node_modules/.bin/sonar-scanner \
		 -Dsonar.login=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
      -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn

.PHONY: test
test: ## Runs jest tests (delegates to test target in package.json)
	yarn test

.PHONY: coverage # important since we also have a subfolder 'coverage'
coverage: ## Runs jest tests with coverage and open report
	yarn test:coverage
	open coverage/index.html
