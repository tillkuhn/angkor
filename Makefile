# Inspired by https://github.com/pgporada/terraform-makefile
# quickref: https://www.gnu.org/software/make/manual/html_node/Quick-Reference.html
.DEFAULT_GOAL := help # default target when launched without arguments
.ONESHELL:
.SHELL := /usr/bin/bash
.PHONY: ec2-start ec2-stop ec2-status ssh infra-init infra-plan infra-apply api-deploy ui-deploy help
.SILENT: ec2-status help # no preceding @s needed
.EXPORT_ALL_VARIABLES: # especially important for sub-make calls

AWS_PROFILE = timafe
ENV_FILE ?= ~/.angkor/.env
AWS_CMD ?= aws
SSH_OPTIONS ?= -o StrictHostKeyChecking=no

# https://unix.stackexchange.com/questions/269077/tput-setaf-color-table-how-to-determine-color-codes
BOLD=$(shell tput bold)
RED=$(shell tput setaf 1)
GREEN=$(shell tput setaf 2)
YELLOW=$(shell tput setaf 3)
CYAN=$(shell tput setaf 6)
RESET=$(shell tput sgr0)
STARTED=$(shell date +%s)

############################
# self documenting makefile recipe: https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
############################
help:
	for PFX in api ui infra ec2 docs tools all ang rel; do \
  		grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\
  	done

############################
# infra tasks for terraform
############################
infra-init: ## Runs terraform init on working directory ./infra, switch to
	@$(MAKE) -C infra init;
	@echo "🏗️ $(GREEN)Terraform Infrastructure successfully initialized $(RESET)[$$(($$(date +%s)-$(STARTED)))s] "

infra-plan: ## Runs terraform plan with implicit init and fmt (alias: plan)
	@$(MAKE) -C infra plan;
	@echo "🏗️ $(GREEN)Infrastructure Infrastructure succcessfully planned $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

infra-deploy: ## Runs terraform apply with auto-approval (alias: apply)
	@$(MAKE) -C infra deploy;
	@echo "🏗️ $(GREEN)Terraform Infrastructure succcessfully deployed $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

# infra task aliases
apply: infra-deploy
plan: infra-plan

##############################
# api backend tasks for gradle
##############################
api-clean: ## Cleans up ./api/build folder
	@$(MAKE) -C api clean;

api-build: ## Assembles backend jar in ./api/build with gradle (alias: assemble)
	@$(MAKE) -C api build;
	@echo "🌇 $(GREEN) Successfully build API jar $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

api-test: ## Runs spring boot unit and integration tests in ./api
	@$(MAKE) -C api test; $(MAKE) -C api lint
	@echo "🌇 $(GREEN) API Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

api-run: ## Runs springBoot API in ./api using gradle bootRun (alias: bootrun)
	@$(MAKE) -C api run
	@# gradle bootRun  --args='--spring.profiles.active=dev'

api-mock: ## Runs OIDC (and potentially other) mock service for api
	docker-compose -f tools/mock-oidc/docker-compose.yml up --detach

# Check resulting image with docker run -it --entrypoint bash angkor-api:latest
# Deprecated, now handled by Github CI Actions
_api-dockerize: .docker_checkrunning api-build ## Builds API docker images on top of recent opdenjdk
	cd api; docker build --build-arg FROM_TAG=jre-14.0.1_7-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-api:latest .
	@# docker tag angkor-api:latest angkor-api:$(shell git describe --abbrev=0) # optional

# # Deprecated, now handled by Github CI Actions
_api-push: api-dockerize .docker_login ## Build and tags API docker image, and pushes to dockerhub
	docker tag angkor-api:latest $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	docker push $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)/angkor-api:latest
	@echo "🐳 $(GREEN)Pushed API image to dockerhub, seconds elapsed $(RESET)[$$(($$(date +%s)-$(STARTED)))s] "

api-deploy: ec2-deploy ## Deploys API with subsequent pull and restart of server on EC2

# backend aliases
bootrun: api-run
assemble: api-build

###########################
# frontend tasks yarn / ng
###########################
ui-clean: ## Remove UI dist folder ./ui/dist
	@$(MAKE) -C ui clean

ui-build: ## Run ng build  in ./ui
	@$(MAKE) -C ui build
	@echo "🌇 $(GREEN) Successfully build UI $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-build-prod: ## Run ng build --prod in ./ui
	@$(MAKE) -C ui build-prod
	@echo "🌇 $(GREEN) Successfully build prod optimized UI $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-test: ## Runs chromeHeadless tests in ./ui
	@$(MAKE) -C ui test; $(MAKE) -C ui lint
	@echo "🌇 $(GREEN) UI Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-run: ## Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
	@$(MAKE) -C ui run

# Deprecated, now handled by Github CI Actions
_ui-dockerize: .docker_checkrunning ui-build-prod ## Creates UI docker image based on nginx
	cd ui; docker build  --build-arg FROM_TAG=1-alpine \
           --build-arg LATEST_REPO_TAG=$(shell git describe --abbrev=0) --tag angkor-ui:latest .
	# docker tag angkor-api:latest angkor-ui:$(shell git describe --abbrev=0) #optional
	# Check resulting image with docker run -it --entrypoint ash angkor-ui:latest

# Deprecated, now handled by Github CI Actions
_ui-push: ui-dockerize .docker_login ## Creates UI docker frontend image and deploys to dockerhub
	docker tag angkor-ui:latest $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	docker push  $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)/angkor-ui:latest
	@echo "🐳 $(GREEN)Pushed UI image to dockerhub, seconds elapsed $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

ui-deploy: ec2-deploy ## Deploys UI with subsequent pull and restart of server on EC2

ui-mocks: ## Run json-server on foreground to mock API services for UI (alias: mock)
	@#cd ui; ./mock.sh  # add  --delay 3000 to delay responses in ms
	json-server  --port 8080 --watch --routes ui/json-server/routes.json ui/json-server/db.json
## run locally: docker run -e SERVER_NAMES=localhost -e SERVER_NAME_PATTERN=localhost -e API_HOST=localhost -e API_PORT=8080 --rm tillkuhn/angkor-ui:latest

# frontend aliases
serve: ui-run
open: ui-run
ui: ui-run
mock: ui-mocks

#################################
# docs tasks using antora
#################################
docs-clean: ## Cleanup docs build directory
	rm -rf ./docs/build

docs-build: ## Generate documentation site using antora-playbook.yml
	DOCSEARCH_ENABLED=true DOCSEARCH_ENGINE=lunr antora --stacktrace --fetch --generator antora-site-generator-lunr antora-playbook.yml
	@echo "📃 $(GREEN)Antora documentation successfully generated in ./docs/build $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

docs-push: docs-build ## Generate documentation site and push to s3
	aws s3 sync --delete ./docs/build s3://$(shell grep "^BUCKET_NAME" $(ENV_FILE) |cut -d= -f2-)/deploy/docs
	@echo "📃 $(GREEN)Antora documentation successfully published to s3 $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

docs-deploy: docs-push  ## Deploys docs with subsequent pull and restart of server on EC2 (alias: docs)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) "./appctl.sh deploy-docs"
	@echo "📃 $(GREEN)Antora documentation successfully deployed on server $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

# docs aliases
docs: docs-deploy

#################################
# tools management tasks
#################################
tools-test: ## Run lint and tests (tbi)
	cd tools; $(MAKE) lint
	@echo "🌇 $(GREEN) Tools	 Tests finished $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

tools-deploy: ## Interim task to trigger re-init of tools on server side
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) "./appctl.sh update deploy-tools"
	@echo "📃 $(GREEN)TOols successfully deployed on server $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"


#################################
# ec2 instance management tasks
#################################
ec2-stop:  ## Stops the ec2 instance (alias: stop)
	aws ec2 stop-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

ec2-start:  ## Launches the ec-2instamce (alias: start)
	aws ec2 start-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-)

ec2-status:  ## Get ec2 instance status (alias: status)
	@echo "🖥️ $(GREEN) Current Status of EC2-Instance $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-):$(RESET)";
	@# better: aws ec2 describe-instances --filters "Name=tag:appid,Values=angkor"
	aws ec2 describe-instances --instance-ids $(shell grep "^INSTANCE_ID" $(ENV_FILE) |cut -d= -f2-) --query 'Reservations[].Instances[].State[].Name' --output text

ec2-ps: ## Run docker compose status on instance (alias: ps)
	@ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-) $(SSH_OPTIONS) ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	"docker ps;echo;top -b -n 1 | head -5;systemctl status angkor-sqs"

ec2-login:  ## Exec ssh login into current instance (alias: ssh,login)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-)

ec2-deploy: ## Pull recent config on server, triggers docker-compose up (alias: pull)
	ssh -i $(shell grep "^SSH_PRIVKEY_FILE" $(ENV_FILE) |cut -d= -f2-)  $(SSH_OPTIONS)  ec2-user@$(shell grep "^PUBLIC_IP" $(ENV_FILE) |cut -d= -f2-) \
	    "./appctl.sh update deploy-api deploy-ui deploy-docs deploy-tools"

# ec2- aliases
stop: ec2-stop
start: ec2-start
status: ec2-status
ssh: ec2-login
login: ec2-login
deploy: ec2-deploy
ps: ec2-ps

################################
# combine targets for whole app
################################
all-clean: api-clean ui-clean  ## Clean up build artifact directories in backend and frontend (alias: clean)
all-build: api-build ui-build  ## Builds frontend and backend (alias: build)
all-test: api-test ui-test tools-test ## Builds frontend and backend (alias: build)
all-deploy: api-deploy ui-deploy ## builds and deploys frontend and backend images (alias deploy)

# all aliases
clean: all-clean
build: all-build
test: all-test
deploy: all-deploy

release: ## create final release tag with semtag
	NEWTAG=$(shell semtag getlast); NEWNAME=$(shell terraform -chdir=infra output -raw release_name); \
	echo $$NEWTAG; echo $$NEWNAME; \
	git tag $$NEWTAG $${NEWTAG}^{} -f -m "$$NEWNAME"  -m "Created by make release"
	@echo "Dirty files (if any): $(shell git status --porcelain=v1)"
	@semtag final -s minor -o || exit 42
	@echo "Current release: $(shell git describe --tags --abbrev=0)"
	@echo "release = \"$(shell semtag final -s minor -o)\"" >infra/release.auto.tfvars
	@echo "Next minor release: $(shell cat infra/release.auto.tfvars)"
	@terraform -chdir=infra apply -auto-approve -target=module.release
	@echo "Any key to apply, ctrl-c to exit, auto assume (y)es after 10s"; read -t 10 dummy;
	# to list  git tag -l --format='%(contents)' v0.1.0-beta.1
	# print only first line git tag -n v0.1.0-beta.1  or git tag -l  --format='%(contents)' v0.1.0-beta.1|head -1
	semtag final -s minor
	NEWTAG=$(shell semtag getlast); NEWNAME=$(shell terraform -chdir=infra output -raw release_name); \
	git tag $$NEWTAG $$NEWTAG^{} -f -m $$NEWNAME  -m "Created by make release"

#todo enable dependenceisapideploy uideploy infradeloy
angkor: api-push ui-push docs-push infra-deploy ec2-pull ## The ultimate target - builds and deploys everything 🦄
	@echo "🌇 $(GREEN)Successfully built Angkor $(RESET)[$$(($$(date +%s)-$(STARTED)))s]"

##########################################
# internal shared tasks (prefix with .)
###########################################
.docker_login:
	echo $(shell grep "^docker_token" $(ENV_FILE) |cut -d= -f2-) | docker login --username $(shell grep "^DOCKER_USER" $(ENV_FILE) |cut -d= -f2-)  --password-stdin

# will exit with make: *** [.docker_checkrunning] Error 1 if daemon is not running
.docker_checkrunning:
	@if docker ps -q 2>/dev/null; then \
  		echo "🐳 Docker daemon is running happily"; \
  	else echo "🐳 Docker daemon seems to be offline, please launch!"; exit 1; fi

##########################################
# experimental tasks (undocumented, no ##)
###########################################

.localstack: # start localstack with dynamodb
	SERVICES=s3:4572,dynamodb:8000 DEFAULT_REGION=eu-central-1  localstack --debug start  --host

imagine-run: ## Run imagine locally
	cd tools/imagine; $(MAKE) run
