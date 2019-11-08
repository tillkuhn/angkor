## launch with make -s to su
.DEFAULT_GOAL := help
.ONESHELL:
.SHELL := /usr/bin/bash
#A phony target is one that is not really the name of a file; rather it is just a name for a recipe to be executed when you make an explicit request. There are two reasons to use a phony target: to avoid a conflict with a file of the same name, and to improve performance.
.PHONY: help init plan build-api build-ui
.SILENT: help ## no @s needed
.EXPORT_ALL_VARIABLES:
AWS_PROFILE = timafe

help:
	echo "Usage: make [target]"
	echo "Targets:"
	echo "  init        Inits infrastructure in infra with terraform"
	echo "  plan        Plans infrastructure in infra with terraform"
	echo "  apply       Applies infrastructure in infra with terraform"
	echo "  build-api   Creates runnable jar in api with gradle"
	echo "  build-ui    Creates frontend package in ui with yarn"
	echo "  deploy-ui   Deploys webapp artifacts to s3"

init: ; cd infra; terraform init
plan: ; cd infra; terraform plan
apply: ; cd infra; terraform apply --auto-approve
build-api: ; cd api; gradle assemble
build-ui: ; cd ui; yarn build:prod
deploy-ui:
	aws s3 sync ui/dist/letsgo2 s3://${S3_BUCKET_LOCATION}/ui  --delete --size-only
	aws s3 cp ui/dist/letsgo2/index.html s3://${S3_BUCKET_LOCATION}/ui/index.html
    ## size-only is not good for index.html as the size may not change but the checksum of included scripts does


