.ONESHELL:
.PHONY: help lint format lint
.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

format: ## run goimports, write results to sourcefile
	goimports -w .

# https://github.com/psampaz/go-mod-outdated
outdated: ## show outdated direct dependencies
	go get -u github.com/psampaz/go-mod-outdated
	go list -u -m -json all | go-mod-outdated -direct
	@echo "Run 'go get -u all' to update everything"

update: ## go get -u all
	go get -u all

.KAFKA_SASL_USERNAME_DEV ?= $(shell grep "^app.kafka.sasl-username" ../../kotlin/config/application.properties |cut -d= -f2-)
.KAFKA_SASL_PASSWORD_DEV ?= $(shell grep "^app.kafka.sasl-password" ../../kotlin/config/application.properties |cut -d= -f2-)
run: ## run main app
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
  		 env KAFKA_SASL_USERNAME=$(.KAFKA_SASL_USERNAME_DEV) \
             KAFKA_SASL_PASSWORD=$(.KAFKA_SASL_PASSWORD_DEV) \
             KAFKA_TOPIC_PREFIX=$(.KAFKA_SASL_USERNAME_DEV)- \
	 		 AWS_PROFILE=timafe \
	 		 HEALTHBELLS_INTERVAL=5s \
	 		 HEALTHBELLS_QUIET=false \
	 		 go run main.go; \
  	else echo "AWS_SESSION_TOKEN is present"; exit 1; fi

run-alert: ## run main app with an URL that should trigger an alert
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
	 	AWS_PROFILE=timafe HEALTHBELLS_INTERVAL=5s HEALTHBELLS_QUIET=true HEALTHBELLS_URLS=https://gibtsnett.bla go run main.go; \
  	else echo "AWS_SESSION_TOKEN is present"; exit 1; fi

test: ## test with curl
	curl -Ss http://localhost:8091/
