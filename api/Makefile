.DEFAULT_GOAL := help
.PHONY: help clean test lint run build recreate-db get-üplaces
API_LATEST = http://localhost:8080/api/v1

help: ## this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

clean: ## clean build dir
	rm -rf build

test: ## run test
	gradle test --fail-fast --stacktrace;

dependencies: ## gradle dependencyUpdates
	gradle dependencyUpdates

lint: ## A smarter Dockerfile linter that helps you build best practice Docker images
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

run: ## gradle bootRun
	gradle bootRun

build: ## gradle assemble
	gradle assemble

dockerx: build ## docker cross platform build with buildx
	#  linux/amd64 linux/arm64
	DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/arm64 -t angkor-api .

recreate-db: ## Recreate local dev and test DB
	./recreate-db.sh

get-places: ## Fetch list of places using curl and jq
	curl -Ss $(API_LATEST)/places/search/ | jq .

get-pois: ## Fetch list of places using curl and jq
	curl -Ss $(API_LATEST)/pois/ | jq .
