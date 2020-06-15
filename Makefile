# Inspired by https://github.com/pgporada/terraform-makefile
.DEFAULT_GOAL := help # default target when launched without arguments
.ONESHELL:
.SHELL := /usr/bin/bash
# A phony target is one that is not really the name of a file; rather it is just a name for a recipe
.PHONY: ec2start ec2stop ec2status ssh tfinit tfplan tfapply apideploy uideploy up down clean help
.SILENT: ec2status help ## no preceding @s needed
.EXPORT_ALL_VARIABLES:
AWS_PROFILE = timafe
ENV_FILE = .env
AWS_CMD ?= aws
BOLD=$(shell tput bold)
RED=$(shell tput setaf 1)
GREEN=$(shell tput setaf 2)
YELLOW=$(shell tput setaf 3)
RESET=$(shell tput sgr0)

# self documenting makefile recipe: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
help:
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

# manage infra
tfinit: ## terraform init
	cd infra; terraform init
tfplan: ## terraform plan (alias: plan)
	cd infra; terraform fmt; terraform init; terraform validate; terraform plan
tfapply: ## terraform apply (alias: apply)
	cd infra; terraform apply --auto-approve
apply: tfapply
plan: tfplan

# manage ec2
ec2stop:  ## stops the ec2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2)
ec2start:  ## launches the ec-2instamce (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2)
ec2status:  ## get ec2 instance status (alias: status)
	echo "$(BOLD)$(GREEN) Current Status of EC2-Instance $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2):$(RESET)";
	aws ec2 describe-instances --instance-ids $(shell grep "^instance_id" $(ENV_FILE) |cut -d= -f2) --query 'Reservations[].Instances[].State[].Name' --output text
stop: ec2stop
start: ec2start
status: ec2status

## todo aws ec2 describe-instances --filters "Name=tag:Name,Values=MyInstance"
#login: ; ssh -i mykey.pem -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" terraform/local/setenv.sh |cut -d= -f2)

ssh:  ## ssh logs into current instance (alias: login)
	ssh -i angkor.pem -o StrictHostKeyChecking=no ec2-user@$(shell grep "^public_ip" $(ENV_FILE) |cut -d= -f2)

apibuild: ## runs gradle daemon to assemble backend jar
	gradle assemble


uibuild: ## builds ui --prod
	cd ui; ng build --prod

apideploy: ## build api docker image and deploys to dockerhub
	cd .; docker build -t angkor-api:latest .
	docker tag angkor-api:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-api:latest
	docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2)
	docker push $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-api:latest

uideploy: ## build ui docker image and deploys to dockerhub
	cd ui; docker build -t angkor-ui:latest .
	docker tag angkor-ui:latest $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-ui:latest
	docker login --username $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2) --password $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2)
	docker push  $(shell grep "^docker_user" $(ENV_FILE) |cut -d= -f2)/angkor-ui:latest

deploy: apideploy uideploy ## builds images for both api and ci and deploys

up: ## runs docker-compose up to start all services in detached mode
	docker-compose up --detach

down: ## runs docker-compose down to show down all services
	docker-compose down

clean:  ## Clean up build artifacts (gradle + npm)
	rm -rf ui/dist
	rm -rf ./build

#backend-build: ; cd backend; gradle assemble
#backend-run: ; cd backend; gradle bootRun
#ui-build: ; cd ui; yarn build:prod
#ui-run: ; cd ui; yarn start --open
#json-server: ; cd ui; ./mock.sh

#todo aws ec2 describe-instances --filters "Name=tag:Name,Values=MyInstance"
#  aws ec2 describe-instances --filters "Name=tag:appid,Values=letsgo2" --query "Reservations[].Instances[].InstanceId"

# https://github.com/localstack/localstack/blob/master/Makefile get inspired
#help:
#	echo "Usage: make [target]"
#	echo "Targets:"
#	echo "  init        Inits infrastructure in infra with terraform"
#	echo "  plan        Plans infrastructure in infra with terraform"
#	echo "  apply       Applies infrastructure in infra with terraform"
#	echo "  backend-build   Creates runnable jar in backend with gradle"
#	echo "  backend-deploy  Deploys app.jar to s3"
#	echo "  backend-run     Runs spring boot app in backend"
#	echo "  ui-build    Creates frontend package in ui with yarn"
#	echo "  ui-deploy   Deploys webapp artifacts to s3"
#	echo "  ui-run      Runs user interface"
#	echo "  localstack  Runs dynambodb / s3 mocks for spring boot"
#	echo "  json-server Runs json-server to mock rest backend for ui"
#	echo "  clean       Cleanup build / dist directories"
#mock: ; SERVICES=s3:4572,dynamodb:8000 PORT_WEB_UI=8999 DEBUG=1 DATA_DIR=$(PWD)/mock/data localstack start --host
#localstack: ## start localstack with dynamodb
#	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host

