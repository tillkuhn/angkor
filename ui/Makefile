.DEFAULT_GOAL := help
.PHONY: help lint recreate-db run test mock check-update

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

lint: ## A smarter Dockerfile linter that helps you build best practice Docker images
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

scratch: ## run typescript scratchfile
	ts-node scratch.ts

scra: scratch

run: ## build and open landing page in browser
	yarn open

test: ## run karma tests in chrome headless mode
	yarn test:headless

mock: ## run json-server to mock api in port 8080
	json-server --port 8080 --watch --routes json-server/routes.json json-server/db.json

check-update: ## run ng-update
	ng update

