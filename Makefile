# Inspired by https://github.com/pgporada/terraform-makefile
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

# self documenting makefile recipe: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
help:
	grep -E  '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'; \

# manage infrastructure with terraform
tfinit: ## runs terraform init
	cd infra; terraform init
tfplan: ## runs terraform plan with implicit init and fmt (alias: plan)
	cd infra; terraform fmt; terraform init; terraform validate; terraform plan
tfapply: ## runs terraform apply with auto-approval (alias: apply)
	cd infra; terraform apply --auto-approve
# terraform aliases
apply: tfapply
plan: tfplan

# manage server ec2 instance
ec2stop:  ## stops the ec2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2)
ec2start:  ## launches the ec-2instamce (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2)
ec2status:  ## get ec2 instance status (alias: status)
	echo "$(BOLD)$(GREEN) Current Status of EC2-Instance $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2):$(RESET)";
	# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2) --query 'Reservations[].Instances[].State[].Name' --output text
ec2login:  ## ssh logs into current instance (alias: ssh)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2)
ec2pull: ## pulls recent config and changes on server side, triggers docker-compose up (alias: pull)
	ssh -i $(shell grep "^ssh_privkey_file" $(ENV_FILE) |cut -d= -f2) -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2) ./deploy.sh
# ec2 aliases
stop: ec2stop
start: ec2start
status: ec2status
ssh: ec2login
pull: ec2pull

# backend tasks
apiclean: ## cleans up build/ folder in api
	rm -rf build

apibuild: ## assembles backend jar with gradle (alias: assemble)
	gradle assemble

apirun: ## runs springBoot app using gradle bootRun (alias: bootrun)
	gradle bootRun
	# gradle bootRun  --args='--spring.profiles.active=dev'

apideploy: apibuild ## build api docker image and deploys to dockerhub
	cd .; docker build -t angkor-api:latest .
	docker tag angkor-api:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-api:latest
	docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2)
	docker push $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-api:latest
apirollout: apideploy ec2pull ## deploy api with subsequent pull and restart on server
# backend aliases
bootrun: apirun
assemble: apibuild

# frontend tasks
uiclean: ## cleans up dist/ folder in ui
	rm -rf ui/dist
uibuild: ## builds ui
	cd ui; ng build --prod
uibuild-prod: ## builds ui --prod
	cd ui; ng build --prod
uirun: ## runs ui with ng serve and opens browser (alias: serve)
	cd ui; ng serve --open
uideploy: uibuild-prod ## build ui prod, creates docker image and deploys to dockerhub
	cd ui; docker build -t angkor-ui:latest .
	docker tag angkor-ui:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-ui:latest
	docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2)
	docker push  $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-ui:latest
uirollout: uideploy ec2pull ## deploy ui with subsequent pull and restart on server

uimock: ## runs mockapi server for frontend
	cd ui; ./mock.sh
## run locally: docker run -e SERVER_NAMES=localhost -e SERVER_NAME_PATTERN=localhost -e API_HOST=localhost -e API_PORT=8080 --rm tillkuhn/angkor-ui:latest
# frontend aliases
serve: uirun

# combine targets for whole app
allclean: apiclean uiclean  ## Clean up build artifact directories in backend and frontend (alias: clean)
allbuild: apibuild uibuild  ## Builds frontend and backend (alias: build)
alldeploy: apideploy uideploy ## builds and deploys frontend and backend images (alias deploy)
# all aliases
clean: allclean
build: allbuild
deploy: alldeploy

# experimental tasks (no ## comment)
.localstack: # start localstack with dynamodb
	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host

.up: # runs docker-compose up to start all services in detached mode
	docker-compose up --detach

.down: # runs docker-compose down to show down all services
	docker-compose down
