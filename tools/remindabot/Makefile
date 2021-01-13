GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: help lint format lint
.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## run goimports, write results to sourcefile
	goimports -w .

run: ## run main app
	env SOMEVAR=true go run main.go
