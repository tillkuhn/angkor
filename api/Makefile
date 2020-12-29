.DEFAULT_GOAL := help
.PHONY: help lint  recreate-db

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

lint: ## A smarter Dockerfile linter that helps you build best practice Docker images
	@echo "Linting ${CURDIR}/Dockerfile"; hadolint Dockerfile

recreate-db: ## Recreate local dev DB
	./recreate-db.sh

get-places: ## Fetch list of places using curl and ja
	curl -Ss localhost:8080/api/v1/places|jq .

