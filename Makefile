# Inspired by https://github.com/pgporada/terraform-makefile
# quickref: https://www.gnu.org/software/make/manual/html_node/Quick-Reference.html
.DEFAULT_GOAL := help # default target when launched without arguments
.ONESHELL:
.SHELL := /usr/bin/bash
.PHONY: ec2start ec2stop ec2status ssh tfinit tfplan tfapply apideploy uideploy up down clean help
.SILENT: ec2status help ## no preceding @s needed
.EXPORT_ALL_VARIABLES:

AWS_PROFILE = timafe
ENV_FILE ?= .env
AWS_CMD ?= aws
BOLD=$(shell tput bold)
RED=$(shell tput setaf 1)
GREEN=$(shell tput setaf 2)
YELLOW=$(shell tput setaf 3)
RESET=$(shell tput sgr0)
STARTED=$(shell date +%s)

# self documenting makefile recipe: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
help:
	for P in api ui tf ec2 docs all ang; do grep -E "^$$P[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'; echo ""; done

############################
# infra tasks for terraform
#############################
tfinit: ## runs terraform init
	cd infra; terraform init

tfplan: ## runs terraform plan with implicit init and fmt (alias: plan)
	cd infra; terraform fmt; terraform init; terraform validate; terraform plan

tfdeploy: ## runs terraform apply with auto-approval (alias: apply)
	cd infra; terraform apply --auto-approve
	@echo "Deployed infra, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

# terraform aliases
apply: tfdeploy
plan: tfplan

###############################
# api backend tasks for gradle
###############################
apiclean: ## cleans up build/ folder in api
	rm -rf api/build

apibuild: ## assembles backend jar with gradle (alias: assemble)
	cd api; gradle assemble
	@echo "Built api, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

apirun: ## runs springBoot app using gradle bootRun (alias: bootrun)
	cd api; gradle bootRun
	# gradle bootRun  --args='--spring.profiles.active=dev'

apidockerize: apibuild ## builds api docker images on top of recent opdenjdk
	cd api; docker build --build-arg FROM_TAG=jre-14.0.1_7-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-api:latest .
	# docker tag angkor-api:latest angkor-api:$(shell git describe --abbrev=0) # optional
    # Check resulting image with docker run -it --entrypoint bash angkor-api:latest

apipush: apidockerize ## build api docker image and deploys to dockerhub
	docker tag angkor-api:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	@docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2-)
	docker push $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	@echo "Pushed api, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

apideploy: apipush ec2pull ## deploy api with subsequent pull and restart on server

# backend aliases
bootrun: apirun
assemble: apibuild

###########################
# frontend tasks yarn / ng
###########################
uiclean: ## cleans up dist/ folder in ui
	rm -rf ui/dist

uibuild: ## builds ui
	cd ui; ng build --prod
	@echo "Built ui, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

uibuild-prod: ## builds ui --prod
	cd ui; ng build --prod

uirun: ## runs ui with ng serve and opens browser (alias: serve)
	cd ui; ng serve --open

uidockerize: uibuild-prod ## creates frontend docker image based on nginx
	cd ui; docker build  --build-arg FROM_TAG=1-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-ui:latest .
	# docker tag angkor-api:latest angkor-ui:$(shell git describe --abbrev=0) #optional
	# Check resulting image with docker run -it --entrypoint ash angkor-ui:latest

uipush: uidockerize ## creates docker frontend image and deploys to dockerhub
	docker tag angkor-ui:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	@docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2-)
	docker push  $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	@echo "Pushed ui, $$(($$(date +%s)-$(STARTED))) seconds elapsed 🌇"

uideploy: uipush ec2pull ## deploy ui with subsequent pull and restart on server

uiimocks: ## runs json-server to mock api, auth and other services on which ui depends
	cd ui; ./mock.sh
## run locally: docker run -e SERVER_NAMES=localhost -e SERVER_NAME_PATTERN=localhost -e API_HOST=localhost -e API_PORT=8080 --rm tillkuhn/angkor-ui:latest

# frontend aliases
serve: uirun

#################################
# ec2 instance management tasks
#################################
ec2stop:  ## stops the ec2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-)

ec2start:  ## launches the ec-2instamce (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-)

ec2status:  ## get ec2 instance status (alias: status)
	echo "$(BOLD)$(GREEN) Current Status of EC2-Instance $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-):$(RESET)";
	# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2-) --query 'Reservations[].Instances[].State[].Name' --output text

ec2ps: ## show docker compose status on instance
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-) docker ps

ec2login:  ## ssh logs into current instance (alias: ssh)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-)

ec2pull: ## pulls recent config and changes on server side, triggers docker-compose up (alias: pull)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2-) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2-) ./deploy.sh

# ec2 aliases
stop: ec2stop
start: ec2start
status: ec2status
ssh: ec2login
pull: ec2pull

#################################
# docs tasks using antora
#################################
docsbuild: ## antora generate antora-playbook.yml
	antora generate antora-playbook.yml

docsdeploy: ## deploys antora built html pages to s3
	@echo to be implemented

################################
# combine targets for whole app
################################
allclean: apiclean uiclean  ## Clean up build artifact directories in backend and frontend (alias: clean)
allbuild: apibuild uibuild  ## Builds frontend and backend (alias: build)
alldeploy: apideploy uideploy ## builds and deploys frontend and backend images (alias deploy)

# all aliases
clean: allclean
build: allbuild
deploy: alldeploy

#todo enable dependenceisapideploy uideploy tfdeloy
angkor: apipush uipush tfdeploy ec2pull ##  the ultimate target - builds and deploys everything 🦄
	@echo "Built Angkor 🌇"

##########################################
# experimental tasks (undocumented, no ##)
###########################################
.localstack: # start localstack with dynamodb
	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host

.up: # runs docker-compose up to start all services in detached mode
	docker-compose up --detach

.down: # runs docker-compose down to show down all services
	docker-compose down
