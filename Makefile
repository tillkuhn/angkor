## launch with make -s to su
.DEFAULT_GOAL := help
.ONESHELL:
.SHELL := /usr/bin/bash
#A phony target is one that is not really the name of a file; rather it is just a name for a recipe to be executed when you make an explicit request. There are two reasons to use a phony target: to avoid a conflict with a file of the same name, and to improve performance.
.PHONY: help init plan build-api build-ui
.SILENT: help init plan build-ui ## no @s needed
.EXPORT_ALL_VARIABLES:
AWS_PROFILE = timafe

help:
	@echo "useage make hi or ho"

init: ; cd infra; terraform init
plan: ; cd infra; terraform plan

build-api:
	@gradle assemble 

build-ui:
	cd ui; pwd


