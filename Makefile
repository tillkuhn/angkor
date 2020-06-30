# Inspired by https://github.com/pgporada/terraform-makefile
# quickref: https://www.gnu.org/software/make/manual/html_node/Quick-Reference.html
.DEFAULT_GOAL := help # default target when launched without arguments
.ONESHELL:
.SHELL := /usr/bin/bash
.PHONY: ec2-start ec2-stop ec2-status ssh infra-init infra-plan infra-apply api-deploy ui-deploy help
.SILENT: ec2-status help ## no preceding @s needed
.EXPORT_ALL_VARIABLES:

AWS_PROFILE = timafe
ENV_FILE ?= .env
AWS_CMD ?= aws

# https://unix.stackexchange.com/questions/269077/tput-setaf-color-table-how-to-determine-color-codes
BOLD=$(shell tput bold)
RED=$(shell tput setaf 1)
GREEN=$(shell tput setaf 2)
YELLOW=$(shell tput setaf 3)
CYAN=$(shell tput setaf 6)
RESET=$(shell tput sgr0)
STARTED=$(shell date +%s)

############################
# self documenting makefile recipe: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
############################
help:
	for PFX in api ui infra ec2 docs all ang; do \
  		grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\
  	done

############################
# infra tasks for terraform
#############################
infra-init: ## Runs terraform init on working directory ./infra
	cd infra; terraform init
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🏗️ Terraform successfully initialized"

infra-plan: infrainit ## Runs terraform plan with implicit init and fmt (alias: plan)
	cd infra; terraform fmt; terraform validate; terraform plan
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🏗️ Infrastructure succcessfully planned"

infra-deploy: ## Runs terraform apply with auto-approval (alias: apply)
	cd infra; terraform apply --auto-approve
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🏗️ $(GREEN)Terraform Infrastructure succcessfully deployed$(RESET)"

# terraform aliases
apply: infra-deploy
plan: infra-plan

###############################
# api backend tasks for gradle
##############################
api-clean: ## Cleans up ./api/build folder
	rm -rf api/build

api-build: ## Assembles backend jar in ./api/build with gradle (alias: assemble)
	cd api; gradle assemble
	@echo "Built api, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

api-run: ## Runs springBoot API in ./api using gradle bootRun (alias: bootrun)
	cd api; gradle bootRun
	@# gradle bootRun  --args='--spring.profiles.active=dev'

# Check resulting image with docker run -it --entrypoint bash angkor-api:latest
api-dockerize: .docker_checkrunning api-build ## Builds API docker images on top of recent opdenjdk
	cd api; docker build --build-arg FROM_TAG=jre-14.0.1_7-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-api:latest .
	@# docker tag angkor-api:latest angkor-api:$(shell git describe --abbrev=0) # optional

api-push: api-dockerize .docker_login ## Build and tags API docker image, and pushes to dockerhub
	docker tag angkor-api:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	docker push $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🐳 $(GREEN)Pushed API image to dockerhub, seconds elapsed$(RESET)"

api-deploy: api-push ec2-pull ## Deploys API with subsequent pull and restart of server on EC2

# backend aliases
bootrun: api-run
assemble: api-build

###########################
# frontend tasks yarn / ng
###########################
ui-clean: ## Remove UI dist folder ./ui/dist
	rm -rf ui/dist

ui-build: ## Run ng build  in ./ui
	cd ui; ng build
	@echo "Built UI, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

ui-build-prod: ## Run ng build  in ./ui
	cd ui; ng build --prod

ui-run: ## Run UI with ng serve and opens UI in browser (alias: serve,open)
	cd ui; ng serve --open

ui-dockerize: .docker_checkrunning ui-build-prod ## Creates UI docker image based on nginx
	cd ui; docker build  --build-arg FROM_TAG=1-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-ui:latest .
	# docker tag angkor-api:latest angkor-ui:$(shell git describe --abbrev=0) #optional
	# Check resulting image with docker run -it --entrypoint ash angkor-ui:latest

ui-push: ui-dockerize .docker_login ## Creates UI docker frontend image and deploys to dockerhub
	docker tag angkor-ui:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	docker push  $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🐳 $(GREEN)Pushed UI image to dockerhub, seconds elapsed$(RESET)"

ui-deploy: ui-push ec2-pull ## Deploys UI with subsequent pull and restart of server on EC2

ui-mocks: ## Run json-server on foreground to mock API services for UI (alias: mock)
	@#cd ui; ./mock.sh
	json-server  --port 8080 --watch --routes ui/server/routes.json ui/server/db.json
## run locally: docker run -e SERVER_NAMES=localhost -e SERVER_NAME_PATTERN=localhost -e API_HOST=localhost -e API_PORT=8080 --rm tillkuhn/angkor-ui:latest

# frontend aliases
serve: ui-run
open: ui-run
mock: ui-mocks

#################################
# docs tasks using antora
#################################
docs-build: ## Generate documentation site usingantora-playbook.yml (alias: docs)
	antora generate antora-playbook.yml
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 📃 $(GREEN)Antora documentation successfully generated in ./docs/build$(RESET)"

docs-deploy: docs-build ## Generate documentation site tp s3
	aws s3 sync ./docs/build s3://$(shell grep "^bucket_name" $(ENV_FILE) |cut -d= -f2-)/docs
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 📃 $(GREEN)Antora documentation successfully published to s3$(RESET)"

# docs aliases
docs: docs-deploy

#################################
# ec2 instance management tasks
#################################
ec2-stop:  ## Stops the ec2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-)

ec2-start:  ## Launches the ec-2instamce (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-)

ec2-status:  ## Get ec2 instance status (alias: status)
	@echo "🖥️ $(GREEN) Current Status of EC2-Instance $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-):$(RESET)";
	@# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-) --query 'Reservations[].Instances[].State[].Name' --output text

ec2-ps: ## Run docker compose status on instance (alias: ps)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2--user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-) docker ps

ec2-login:  ## Exec ssh login into current instance (alias: ssh)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2--user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-)

ec2-pull: ## Pull recent config on server, triggers docker-compose up (alias: pull)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-) ./deploy.sh

# ec2- aliases
stop: ec2-stop
start: ec2-start
status: ec2-status
ssh: ec2-login
pull: ec2-pull
ps: ec2-ps


################################
# combine targets for whole app
################################
all-clean: api-clean ui-clean  ## Clean up build artifact directories in backend and frontend (alias: clean)
all-build: api-build ui-build  ## Builds frontend and backend (alias: build)
all-deploy: api-deploy ui-deploy ## builds and deploys frontend and backend images (alias deploy)

# all aliases
clean: all-clean
build: all-build
deploy: all-deploy

#todo enable dependenceisapideploy uideploy infradeloy
angkor: api-push ui-push infra-deploy ec2-pull ## The ultimate target - builds and deploys everything 🦄
	@echo "[$$(($$(date +%s)-$(STARTED)))s] 🌇 $(GREEN)Successfully built Angkor$(RESET)"

##########################################
# internsl shared tasks (prefix with .)
###########################################
.docker_login:
	echo $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2-) | docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)  --password-stdin

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

.up: # runs docker-compose up to start all services in detached mode
	docker-compose up --detach
    # docker-compose down
