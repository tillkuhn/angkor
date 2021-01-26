.ONESHELL:
.PHONY: help format fmt lint run dryrun dry
.DEFAULT_GOAL := help

CERTBOT_DOMAIN_NAME:=$(shell grep "^CERTBOT_DOMAIN_NAME" ~/.angkor/.env |cut -d= -f2-)

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## run goimports, write results to sourcefile (alias fmt)
	goimports -w .; go fmt .

fmt: format ## alias for format

test: ## run go tests
	go test

run: ## run main app
	env SOMEVAR=true go run main.go mailer.go template.go # -envfile /Users/tillkuhn/.angkor/.env

runext: ## run  with external API Endpoint
	env REMINDABOT_API_URL=https://$(CERTBOT_DOMAIN_NAME)/api/v1/notes/reminders \
        go run main.go mailer.go template.go model.go

dryrun: ## dryrun (alias dry)
	env REMINDABOT_SMTP_DRYRUN=true go run main.go mailer.go template.go

dryext: ## dryrun  with external API Endpoint
	env REMINDABOT_SMTP_DRYRUN=true REMINDABOT_API_URL=https://$(CERTBOT_DOMAIN_NAME)/api/v1/notes/reminders \
        go run main.go mailer.go template.go

dry403: ## dryrun (alias dry) produces a 403
	env REMINDABOT_SMTP_DRYRUN=true REMINDABOT_API_TOKEN=nixexpect403 go run main.go mailer.go template.go

dry: dryrun
