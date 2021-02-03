.DEFAULT_GOAL := help
.PHONY: help
.EXPORT_ALL_VARIABLES: # especially important for sub-make calls
AWS_PROFILE ?= timafe

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

tfswitch: ## runs tfswitch if exists and .terraform-version is present
	test -f .terraform-version && hash tfswitch 2>/dev/null && tfswitch;

init: tfswitch ## terraform init
	test -f .terraform-version && hash tfswitch 2>/dev/null && tfswitch; terraform init

fmt: tfswitch ## terraform fmt
	terraform fmt

check: tfswitch ## terraform fmt -check
	terraform fmt -check

plan: tfswitch fmt ## terraform fmt; terraform validate; terraform plan
	terraform validate; terraform plan

apply: tfswitch	fmt ## terraform apply --auto-approve
	terraform apply --auto-approve

deploy: apply ## alias for apply

release: ## manage release info based on latest git tag
	terraform apply -auto-approve -target=module.release
	terraform output -raw release_name
