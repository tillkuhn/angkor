.DEFAULT_GOAL := help
.PHONY: help
.EXPORT_ALL_VARIABLES: # especially important for sub-make calls

AWS_PROFILE ?= timafe
ENV_FILE ?= ~/.angkor/.env
SSH_OPTIONS ?= -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null

help:
	@grep -E '^[a-zA-Z_0-9-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

# Terraform Lifecycle

tfswitch: ## Run 'tfswitch' utility if the binary exists and versions.tf is present
	test -f versions.tf && hash tfswitch 2>/dev/null && tfswitch;

init: tfswitch ## Run 'terraform init' to prepare your working directory for other commands
	terraform init

plan: tfswitch fmt ## Run 'terraform plan', implies includes targets fmt and validate
	terraform validate; terraform plan

apply: tfswitch	fmt ## Run 'terraform apply --auto-approve' to apply changes without prompting
	terraform apply --auto-approve

fmt: tfswitch ## Run 'terraform fmt -recursive' to reformat config in the the standard style
	terraform fmt -recursive

check: tfswitch ## Run 'terraform fmt -check' to check if input is formatted, return != 0 if not
	terraform fmt -check

update: ## Run 'terraform init -upgrade' to update provider versions
	terraform init -upgrade
	tfswitch --latest
	@echo "Finished, it's recommended to run 'make plan' now to verify if everything is still working "

upgrade: update ## Alias or update

lock: ## Ensure lock file contains suitable provider checksums on all of selected platforms.
	terraform providers lock -platform=darwin_amd64 -platform=linux_amd64 -platform=linux_arm64

release: ## Manage release info based on latest git tag and release.auto.tfvars
	terraform apply -auto-approve -target=module.release
	terraform output -raw release_name

# EC2 Instance Managements Tasks

ec2-stop:  ## Stop the EC2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

ec2-start:  ## Launch the EC2 instance (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

ec2-status:  ## Retrieve EC2 instance status (alias: status)
	@echo "🖥️ $(GREEN) Current Status of EC2-Instance $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-):$(RESET)";
	@# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-) --query 'Reservations[].Instances[].State[].Name' --output text

ec2-ps: ## Run docker compose status on instance (alias: ps)
	@ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-) $(SSH_OPTIONS) ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	"docker ps;echo;top -b -n 1 | head -5;systemctl status polly"

ec2-login:  ## Exec ssh login into current instance (alias: ssh, login)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-)

ec2-deploy: ## Pull recent config on server, triggers docker-compose up (alias: pull)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	    "./appctl.sh update deploy-api deploy-ui deploy-docs deploy-tools"

# aliases & shortcuts
ssh: ec2-login ## Alias for ec2-login
ps: ec2-ps ## Alias for ec2-ps
deploy: apply ## Alias for deploy
