GITLAB_TOKEN :=$(shell cat ~/.gitlab_token)
.ONESHELL:
.PHONY: format run lint

# only for local run and test, see ../Makefile for common tools build
format:
	goimports -w .

run:
	@if [ -z $(AWS_SESSION_TOKEN) 	]; then \
	 	AWS_PROFILE=timafe HEALTHBELLS_INTERVAL=5s go run main.go; \
  	else echo "AWS_SESSION_TOKEN is present"; exit 1; fi

test:
	curl -Ss http://localhost:8091/
