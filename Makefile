# Root Module Makefile for Project Angkor
# Inspired by https://github.com/pgporada/terraform-makefile
# Quick Reference: https://www.gnu.org/software/make/manual/html_node/Quick-Reference.html

# https://unix.stackexchange.com/questions/269077/tput-setaf-color-table-how-to-determine-color-codes
# DISABLE; will generate Makefile:11: not recursively expanding RESET to export to shell function
#BOLD=$(shell tput bold)
#RED=$(shell tput setaf 1)
#GREEN=$(shell tput setaf 2)
#YELLOW=$(shell tput setaf 3)
#CYAN=$(shell tput setaf 6)
#RESET=$(shell tput sgr0)
#STARTED=$(shell date +%s)

.DEFAULT_GOAL := help # default target when launched without arguments
.EXPORT_ALL_VARIABLES: # especially important for sub-make calls
.ONESHELL:
.PHONY: ec2-start ec2-stop ec2-status ssh tf-init tf-plan tf-apply api-deploy ui-deploy help
.SHELL := /usr/bin/bash
.SILENT: help # no preceding @s needed to supress output

AWS_PROFILE = timafe
AWS_CMD ?= aws
# Our fingerprint changes rather often
SSH_OPTIONS ?= -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null



############################
# self documenting makefile, recipe:
# https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
############################
# banner: thanks https://manytools.org/hacker-tools/ascii-banner/
help:
	echo
	echo "  █████╗ ███╗   ██╗ ██████╗ ██╗  ██╗ ██████╗ ██████╗ "
	echo " ██╔══██╗████╗  ██║██╔════╝ ██║ ██╔╝██╔═══██╗██╔══██╗"
	echo " ███████║██╔██╗ ██║██║  ███╗█████╔╝ ██║   ██║██████╔╝"
	echo " ██╔══██║██║╚██╗██║██║   ██║██╔═██╗ ██║   ██║██╔══██╗"
	echo " ██║  ██║██║ ╚████║╚██████╔╝██║  ██╗╚██████╔╝██║  ██║"
	echo " ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝"
	echo
	echo "Available Commands:"
	for PFX in api ui tf ec2 docs tools all ang rel git; do \
  		grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\
  	done

############################
# terraform tasks for terraform
############################
tf-init: ## Runs terraform init on working directory ./terraform, switch to
	@$(MAKE) -C terraform init;
	@echo "🏗️ Terraform Infrastructure successfully initialized[$$(($$(date +%s)-$(STARTED)))s] "

tf-plan: ## Runs terraform plan with implicit init and fmt (alias: plan)
	@$(MAKE) -C terraform plan;
	@echo "🏗️ Terraform Infrastructure successfully planned[$$(($$(date +%s)-$(STARTED)))s]"

tf-apply: ## Runs terraform apply with auto-approval (alias: apply)
	@$(MAKE) -C terraform deploy;
	@echo "🏗️ Terraform Infrastructure succcessfully deployed[$$(($$(date +%s)-$(STARTED)))s]"

ssh: ## Runs terraform init on working directory ./terraform, switch to
	@$(MAKE) -C terraform ec2-login;

# terraform task shortcuts
plan: tf-plan
apply: tf-apply

##############################
# api backend tasks for gradle
##############################
api-clean: ## Cleans up ./kotlin/build folder
	@$(MAKE) -C kotlin clean;

api-build: ## Assembles backend jar in ./api/build with gradle (alias: assemble)
	@$(MAKE) -C api build;
	@echo "🌇 $(GREEN) Successfully build API jar $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

api-test: ## Runs spring boot unit and integration tests in ./kotlin
	@$(MAKE) -C kotlin test; $(MAKE) -C kotlin lint
	@echo "🌇 $(GREEN) API Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

api-run: ## Runs springBoot API in ./kotlin using gradle bootRun (alias: bootrun)
	@$(MAKE) -C kotlin run
	@# gradle bootRun  --args='--spring.profiles.active=dev'

api-mock: ## Runs OIDC (and potentially other) mock service for api
	docker-compose -f tools/mock-oidc/docker-compose.yml up --detach

# backend aliases
bootrun: api-run
assemble: api-build

###########################
# frontend tasks yarn / ng
###########################
ui-clean: ## Remove angular dist folder ./angular/dist
	@$(MAKE) -C angular clean

ui-build: ## Run ng build  in ./ui
	@$(MAKE) -C angular build
	@echo "🌇 $(GREEN) Successfully build UI $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-build-prod: ## Run ng build --prod in ./ui
	@$(MAKE) -C angular build-prod
	@echo "🌇 $(GREEN) Successfully build prod optimized angular $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-test: ## Runs chromeHeadless tests in ./angular
	@$(MAKE) -C angular test; $(MAKE) -C angular lint
	@echo "🌇 $(GREEN) angular Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-run: ## Run angular with ng serve and opens WebUI in browser (alias: serve,open,angular)
	@$(MAKE) -C angular run

ui-mocks: ## Run json-server on foreground to mock API services for UI (alias: mock)
	@#cd angular; ./mock.sh  # add  --delay 3000 to delay responses in ms
	json-server  --port 8080 --watch --routes angular/json-server/routes.json angular/json-server/db.json

# frontend aliases
serve: ui-run
open: ui-run
ui: ui-run
mock: ui-mocks

#################################
# docs tasks using antora
#################################
docs-clean: ## Cleanup docs build directory
	rm -rf ./docs/build

docs-build: ## Generate documentation site using antora-playbook.yml (alias: docs)
	antora antora-playbook.yml
	@echo "📃 $(GREEN)Antora documentation successfully generated in ./docs/build $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

docs-push: docs-build ## Generate documentation site and push to s3
	aws s3 sync --delete ./docs/build s3://$(shell grep "^BUCKET_NAME" $(ENV_FILE) |cut -d= -f2-)/deploy/docs
	@echo "📃 $(GREEN)Antora documentation successfully published to s3 $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

docs-deploy: docs-push  ## Deploys docs with subsequent pull and restart of server on EC2 (alias: docs)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) "./appctl.sh deploy-docs"
	@echo "📃 $(GREEN)Antora documentation successfully deployed on server $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

# docs aliases
docs: docs-build

#################################
# go management tasks
#################################
go-test: ## Run lint and tests (tbi)
	cd go; $(MAKE) test
	@echo "🌇 $(GREEN) Go Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

go-deploy: ## Interim task to trigger re-init of tools on server side
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) "./appctl.sh update deploy-tools"
	@echo "📃 $(GREEN)TOols successfully deployed on server $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"


################################
# combine targets for whole app
################################
all-clean: api-clean ui-clean  ## Clean up build artifact directories in backend and frontend (alias: clean)
all-build: api-build ui-build  ## Builds frontend and backend (alias: build)
all-test: api-test ui-test go-test ## Builds frontend and backend (alias: build)
all-deploy: api-deploy ui-deploy ## builds and deploys frontend and backend images (alias deploy)

# all aliases
clean: all-clean
build: all-build
test: all-test
deploy: all-deploy

release: ## create final release tag with semtag
	echo "Dirty files (if any): $(shell git status --porcelain=v1)"
	@echo "Check for next minor version or exit if diry"; semtag final -s minor -o || exit 42
	@echo "Current release: $(shell git describe --tags --abbrev=0)"
	@echo "release = \"$(shell semtag final -s minor -o)\"" >terraform/release.auto.tfvars
	@echo "Next minor release: $(shell cat terraform/release.auto.tfvars)"
	@terraform -chdir=terraform apply -auto-approve -target=module.release
	# to list  git tag -l --format='%(contents)' v0.1.0-beta.1
	# print only first line git tag -n v0.1.0-beta.1  or git tag -l  --format='%(contents)' v0.1.0-beta.1|head -1
	NEWTAG=$(shell semtag final -s minor -o); NEWNAME=$(shell terraform -chdir=terraform output -raw release_name); \
	git tag -a $$NEWTAG -m $$NEWNAME  -m "Created by make release"; \
	git push origin $$NEWTAG

#todo enable dependenceisapideploy uideploy infradeloy
angkor: api-push ui-push docs-push tf-deploy  ## The ultimate target - builds and deploys everything 🦄
	@echo "🌇 $(GREEN)Successfully built Angkor $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

# use LANG=en_GB to force git to talk english even if the shell has a different LANG
git-clean: ## git cleanup, e.g. delete up stale git branches
	LANG=en_GB git branch --merged| grep -v main | xargs git branch -d
	LANG=en_GB git gc
	LANG=en_GB git remote prune --dry-run origin
	@echo "run 'git remote prune origin' to actually delete branch references to remote branches that do not exist"

##########################################
# internal shared tasks (prefix with .)
###########################################
.docker_login:
	echo $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2-) | docker login --username $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)  --password-stdin

# will exit with make: *** [.docker_checkrunning] Error 1 if daemon is not running
.docker_checkrunning:
	@if docker ps -q 2>/dev/null; then \
  		echo "🐳 Docker daemon is running happily"; \
  	else echo "🐳 Docker daemon seems to be offline, please launch!"; exit 1; fi

##########################################
# experimental tasks (undocumented, no ##)
###########################################

.localstack: # start localstack with dynamodb
	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host
