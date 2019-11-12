## launch with make -s to su
.DEFAULT_GOAL := help
.ONESHELL:
.SHELL := /usr/bin/bash
#A phony target is one that is not really the name of a file; rather it is just a name for a recipe to be executed when you make an explicit request. There are two reasons to use a phony target: to avoid a conflict with a file of the same name, and to improve performance.
.PHONY: help init plan api-build ui-build json-server localstack clean
.SILENT: help ## no @s needed
.EXPORT_ALL_VARIABLES:
AWS_PROFILE = timafe
AWS_CMD ?= aws

# https://github.com/localstack/localstack/blob/master/Makefile get inspired
help:
	echo "Usage: make [target]"
	echo "Targets:"
	echo "  init        Inits infrastructure in infra with terraform"
	echo "  plan        Plans infrastructure in infra with terraform"
	echo "  apply       Applies infrastructure in infra with terraform"
	echo "  api-build   Creates runnable jar in api with gradle"
	echo "  api-deploy  Deploys app.jar to s3"
	echo "  api-run     Runs spring boot app in api"
	echo "  ui-build    Creates frontend package in ui with yarn"
	echo "  ui-deploy   Deploys webapp artifacts to s3"
	echo "  ui-run      Runs user interface"
	echo "  localstack  Runs dynambodb / s3 mocks for spring boot"
	echo "  json-server Runs json-server to mock rest api for ui"
	echo "  clean       Cleanup build / dist directories"

init: ; cd infra; terraform init
plan: ; cd infra; terraform plan
apply: ; cd infra; terraform apply --auto-approve
api-build: ; cd api; gradle assemble
api-run: ; cd api; gradle bootRun
ui-build: ; cd ui; yarn build:prod
ui-run: ; cd ui; yarn start --open
localstack: ; cd api; ./mock.sh
json-server: ; cd ui; ./mock.sh
ui-deploy:
	$(AWS_CMD) s3 sync ui/dist/webapp s3://${S3_BUCKET_LOCATION}/deploy/webapp  --delete --size-only
	$(AWS_CMD) s3 cp ui/dist/webapp/index.html s3://${S3_BUCKET_LOCATION}/deploy//webapp/index.html
    ## size-only is not good for index.html as the size may not change but the checksum of included scripts does

api-deploy:
	$(AWS_CMD) s3 sync api/build/libs/app.jar s3://${S3_BUCKET_LOCATION}/deploy/app.jar

#todo aws ec2 describe-instances --filters "Name=tag:Name,Values=MyInstance"
#  aws ec2 describe-instances --filters "Name=tag:appid,Values=letsgo2" --query "Reservations[].Instances[].InstanceId"
clean:             ## Clean up (gradle + npm artifacts)
	rm -rf ui/dist
	rm -rf api/build
