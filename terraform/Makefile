# Special targets, @see  https://www.gnu.org/software/make/manual/html_node/Special-Targets.html
.DEFAULT_GOAL := help
.EXPORT_ALL_VARIABLES: # export all variables to child processes by default, also important for sub-make calls

# Variable defaults
# Why https://opentofu.org/ instead of terraform? https://www.harness.io/blog/why-you-should-use-opentofu-instead-of-terraform
TF_BIN ?= tofu
AWS_PROFILE ?= timafe
ENV_FILE ?= ~/.angkor/.env
SSH_OPTIONS ?= -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null

# Preconditions such as unversioned files that must exist to run any target
# NOTE: These are always checked when make starts, no need for a dedicated target
# How to ensure Makefile variable is set (or unset) as a prerequisite? https://stackoverflow.com/a/4731504/4292075
# use ifdef to check if an environment var is defined, and ifndef to check it it is not set
ifdef AWS_SESSION_TOKEN
  $(error please unset AWS_SESSION_TOKEN is present which leads to conflichts since we rely on AWS_PROFILE)
endif
ifeq (,$(wildcard terraform.tfvars))
  $(error terraform.tfvars not found and not managed by SCM on purpose, use terraform.tfvars.tmpl as template!)
endif
ifeq (,$(wildcard terraform-backend.tf))
  $(error terraform-backend.tf not found and not managed by SCM on purpose, use terraform-backend.tf.tmpl as template!)
endif

.PHONY: help
help:
	@grep -E '^[a-zA-Z_0-9-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


# Terraform / Tofu Lifecycle

.PHONY: init
init: ## Run 'tofu init' to prepare your working directory for other commands
	$(TF_BIN) --version
	$(TF_BIN) init

.PHONY: plan
plan: fmt ## Run 'tofu plan', implies includes targets fmt and validate
	$(TF_BIN) validate; $(TF_BIN) plan

.PHONY: apply
apply: fmt grafana-wake ## Run 'tofu apply --auto-approve' to apply changes without prompting
	$(TF_BIN) apply --auto-approve

# Grafana Cloud Free stacks get paused/archived after inactivity, which makes the grafana provider fail with 503.
# Token is read from tofu state (secrets_read module) unless GRAFANA_CLOUD_ACCESS_POLICY_TOKEN is set,
# it requires the 'stacks:read' and 'stacks:write' scopes for the restart call.
GRAFANA_STACK ?= $(shell grep '^cognito_auth_domain_prefix' terraform.tfvars | cut -d'"' -f2)
GRAFANA_WAKE_TIMEOUT ?= 900

.PHONY: grafana-wake
grafana-wake: ## Restart the Grafana Cloud stack if it's paused/archived, and wait until it's healthy
	@set -e; \
	url="https://$(GRAFANA_STACK).grafana.net/api/health"; \
	if [ "$$(curl -s -o /dev/null -w '%{http_code}' $$url)" = 200 ]; then echo "🟢 Grafana stack $(GRAFANA_STACK) is up"; exit 0; fi; \
	token="$${GRAFANA_CLOUD_ACCESS_POLICY_TOKEN:-$$(echo 'nonsensitive(module.secrets_read.secrets["GRAFANA_CLOUD_ACCESS_POLICY_TOKEN"])' | $(TF_BIN) console 2>/dev/null | tr -d '"')}"; \
	api="https://grafana.com/api/instances/$(GRAFANA_STACK)"; \
	echo "😴 Grafana stack $(GRAFANA_STACK) is $$(curl -s -H "Authorization: Bearer $$token" $$api | jq -r .status), requesting restart"; \
	curl -sf -X POST -H "Authorization: Bearer $$token" $$api/restart >/dev/null || { echo "❌ Restart request failed (token needs stacks:write scope?)"; exit 1; }; \
	end=$$(( $$(date +%s) + $(GRAFANA_WAKE_TIMEOUT) )); \
	until [ "$$(curl -s -o /dev/null -w '%{http_code}' $$url)" = 200 ]; do \
	  [ $$(date +%s) -lt $$end ] || { echo "❌ Timeout after $(GRAFANA_WAKE_TIMEOUT)s waiting for $$url"; exit 1; }; \
	  echo "⏳ waiting for $$url (status=$$(curl -s -H "Authorization: Bearer $$token" $$api | jq -r .status))"; sleep 15; \
	done; \
	echo "🟢 Grafana stack $(GRAFANA_STACK) is up"

.PHONY: update
update: ## Run 'tofu init -upgrade' to update provider versions, see also 'lock'
	$(TF_BIN) init -upgrade
	#test -f versions.tf && hash tfswitch 2>/dev/null && tfswitch --latest || echo tfswitch not installed
	@echo "Finished, it's recommended to run 'make plan' now to verify if everything is still working "

.PHONY: upgrade
upgrade: update ## Alias for target 'update'

.PHONY: fmt
fmt: ## Run 'tofu fmt -recursive' to reformat config in the the standard style
	$(TF_BIN) fmt -recursive

.PHONY: check
check: ## Run 'tofu fmt -check' to check if input is formatted, return != 0 if not
	$(TF_BIN) fmt -check

.PHONY: lock
lock: ## Ensure tofu lock file contains suitable provider checksums on all of selected platforms
	$(TF_BIN) providers lock -platform=darwin_amd64 -platform=linux_amd64 -platform=linux_arm64

# https://stackoverflow.com/a/76971550/4292075
.PHONY: output
output: ## Run 'tofu output' to show output values from your root module
	$(TF_BIN) output -json | jq 'map_values(.value)'

.PHONY: release
release: ## Manage release info based on latest git tag and release.auto.tfvars
	$(TF_BIN) apply -auto-approve -target=module.release
	$(TF_BIN) output -raw release_name

# EC2 Instance Managements Tasks

.PHONY: ec2-stop
ec2-stop:  ## Stop the EC2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

.PHONY: ec2-start
ec2-start:  ## Launch the EC2 instance (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

.PHONY: ec2-status
ec2-status:  ## Retrieve EC2 instance status (alias: status)
	@echo "🖥️ $(GREEN) Current Status of EC2-Instance $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-):$(RESET)";
	@# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-) --query 'Reservations[].Instances[].State[].Name' --output text

.PHONY: ec2-ps
ec2-ps: ## Run docker compose status on instance (alias: ps)
	@ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-) $(SSH_OPTIONS) ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	"docker ps;echo;top -b -n 1 | head -5"

.PHONY: ec2-login
ec2-login:  ## Exec ssh login into current instance (alias: ssh, login)
	test -s $(ENV_FILE) || { echo "$(ENV_FILE) sort does not exist! Exiting..."; exit 1; }
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-)

.PHONY: ec2-deploy
ec2-deploy: ## Pull recent config on server, trigger docker-compose up  on all services (alias: deploy)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	    "./appctl.sh update deploy-api deploy-ui deploy-docs deploy-tools"

# aliases & shortcuts
.PHONY: ssh
ssh: ec2-login ## Alias for ec2-login

.PHONY: ps
ps: ec2-ps ## Alias for ec2-ps

.PHONY: deploy
deploy: ec2-deploy ## Alias for ec2-deploy
