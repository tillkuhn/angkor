.DEFAULT_GOAL := help
.PHONY: help

help:
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
	ng test --browsers ChromeHeadless --watch=false

run: ## Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
	ng serve --open

mock: ## run json-server to mock api in port 8080
	json-server --port 8080 --watch --routes json-server/routes.json json-server/db.json  --middlewares json-server/middleware.js

check-update: ## run ng-update
	ng version; ng update

