# Module specific Makefile for Frontend / UI / Angular Builds

# Make Special Variables https://www.gnu.org/software/make/manual/html_node/Special-Variables.html
# Sets the default goal to be used if no targets were specified on the command line
.DEFAULT_GOAL := help

# Custom Project Variables
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)

# Make sure src/assets/window-env.js has been provisioned by users (e.g. for mapbox access token)
ifeq (,$(wildcard src/assets/window-env.js))
  $(error src/assets/window-env.js not found and not managed by SCM on purpose, use src/assets/window-env.tmpl.js as template!)
endif

# Targets
.PHONY: help
help: ## Display this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: build
build: ## Run 'ng build' for dev purposes
	npx ng build

.PHONY: build-prod
build-prod: ## Run 'ng build --configuration production' to build prod distribution
	npx ng build --configuration production

.PHONY: clean
clean: ## Just wipe out the distribution folder ./dist
	rm -rf dist

.PHONY: lint
lint: ## Run ESLint tools in --fix mode, also launches hadolint for Dockerfile
	npx ng lint --fix angkor-ui
	@hash hadolint 2>/dev/null && hadolint Dockerfile || echo "hadolint not installed, skipping"

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
	  -Dsonar.token=$(SONAR_TOKEN) -Dsonar.projectVersion=$(RELEASE_VERSION) \
    -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=tillkuhn

.PHONY: mock
mock: ## Run 'yarn mock' which launches json-server to mock api on port 8080
	yarn mock

.PHONY: mock-delay
mock-delay: ## Same as mock, but with artificial delay to test progress bars etc.
	yarn mock:delay

.PHONY: outdated
outdated: ## Run 'ng update' and 'yarn outdated' to show outdated dependencies (shows only, changes nothing)
	@echo "Running ng update, see also https://angular.dev/update-guide"
	npx ng version; npx ng update
	@echo "Let's also ask yarn about outdated packages, please updated manually with 'yarn upgrade <package>'"
	yarn outdated || echo "Outdated packages found"
	@printf "\nTo update Angular use 'npx ng update @angular/core@<major> @angular/cli@<major> ...' one major at a time\n"

# Note: nvm is a shell function and can't be used directly here
.PHONY: update-node
update-node: ## Shows advice how to update node to the latest lts version using nvm
	@echo "Current Node Version: $(shell node --version)   NPM: $(shell npm --version)   .nvmrc: $(shell cat .nvmrc)"
	@echo "Use 'nvm use' to switch to the version from .nvmrc, 'nvm ls-remote --lts' to list latest lts releases"
	@echo "Use 'nvm install x.y.z' to install this version and 'nvm use x.y.z' to use it"
	@echo "use 'nvm alias default x.y.z' to set new default and 'nvm uninstall a.b.c' to old versions"


.PHONY: upi
upi: ## Run 'yarn upgrade-interactive'
	@yarn upgrade-interactive

.PHONY: run
run: ## Run 'ng serve --open' that runs http server on :4200 and opens a browser
	npx ng serve --open

.PHONY: run-prod
run-prod: ## Run 'ng serve --configuration production --open' that runs http server on :4200 and opens a browser
	npx ng serve --configuration production --open

.PHONY: scratch
scratch: ## Run typescript scratch.ts file for quick local tests
	@echo "Running ts-node scratch.ts"
	@ts-node scratch.ts || echo "ts-node failed, try 'yarn global add ts-node' if you have not installed it yet"

.PHONY: scra
scra: scratch ## Alias for scratch
