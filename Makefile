## launch with make -s to su
## self documenting makefile: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.DEFAULT_GOAL := help # when called without arguments
.ONESHELL:
.SHELL := /usr/bin/bash
#A phony target is one that is not really the name of a file; rather it is just a name for a recipe to be executed when you make an explicit request. There are two reasons to use a phony target: to avoid a conflict with a file of the same name, and to improve performance.
.PHONY: backend clean help localstack
.SILENT: help ## no @s needed
.EXPORT_ALL_VARIABLES:
AWS_PROFILE = timafe
AWS_CMD ?= aws

backend: ## runs gradle daemon to assemble backend
	gradle assemble

#mock: ; SERVICES=s3:4572,dynamodb:8000 PORT_WEB_UI=8999 DEBUG=1 DATA_DIR=$(PWD)/mock/data localstack start --host
localstack: ## start localstack with dynamodb
	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host

clean:  ## Clean up build artifacts (gradle + npm)
	rm -rf ui/dist
	rm -rf backend/build

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


init: ; cd infra; terraform init
plan: ; cd infra; terraform plan
apply: ; cd infra; terraform apply --auto-approve
#backend-build: ; cd backend; gradle assemble
backend-run: ; cd backend; gradle bootRun
ui-build: ; cd ui; yarn build:prod
ui-run: ; cd ui; yarn start --open
json-server: ; cd ui; ./mock.sh
ui-deploy:
	$(AWS_CMD) s3 sync ui/dist/webapp s3://${S3_BUCKET_LOCATION}/deploy/webapp  --delete --size-only
	$(AWS_CMD) s3 cp ui/dist/webapp/index.html s3://${S3_BUCKET_LOCATION}/deploy//webapp/index.html
    ## size-only is not good for index.html as the size may not change but the checksum of included scripts does

backend-deploy:
	$(AWS_CMD) s3 sync backend/build/libs/app.jar s3://${S3_BUCKET_LOCATION}/deploy/app.jar

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
