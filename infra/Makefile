.DEFAULT_GOAL := help
.PHONY: help
.EXPORT_ALL_VARIABLES: # especially important for sub-make calls
AWS_PROFILE ?= timafe

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

tfswitch: ## runs tfswitch if the binary exists and .terraform-version is present
	test -f .terraform-version && hash tfswitch 2>/dev/null && tfswitch;

init: tfswitch ## runs terraform init (Prepare your working directory for other commands)
	test -f .terraform-version && hash tfswitch 2>/dev/null && tfswitch; terraform init

fmt: tfswitch ## runs terraform fmt (Reformat your configuration in the standard style)
	terraform fmt

check: tfswitch ## terraform fmt -check (Check if input is formatted, return != 0 if not)
	terraform fmt -check

plan: tfswitch fmt ## runs terraform plan (includes targets fmt and validate)
	terraform validate; terraform plan

apply: tfswitch	fmt ## terraform apply --auto-approve (applies without prompting)
	terraform apply --auto-approve

deploy: apply ## alias for target 'apply'

release: ## manage release info based on latest git tag and release.auto.tfvars
	terraform apply -auto-approve -target=module.release
	terraform output -raw release_name
