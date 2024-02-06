# If the .ONESHELL special target appears anywhere in the makefile 
# then all recipe lines for each target will be provided to a single invocation of the shell.
.ONESHELL:
.DEFAULT_GOAL := help
ENV_FILE ?= ~/.angkor/.env
SONAR_TOKEN ?= $(shell grep "^SONAR_TOKEN" ~/.angkor/.env |cut -d= -f2-)
RELEASE_VERSION ?= $(shell git describe --tags --abbrev=0)
RELEASE_NAME ?= makefile-release # should be passed in through environment, e.g. by github workflow
BREW_TOOLS :=  awscli git go golangci-lint hadolint jq postgresql@12 tfswitch trivy yarn yq

# https://clarkgrubb.com/makefile-style-guide#phony-targets
# "Add each phony target as a prerequisite of .PHONY immediately before the target declaration,
# rather than listing all the phony targets in a single place."
.PHONY: help
help: ## shows this help
	@grep -E "^$$PFX[0-9a-zA-Z_-]+:.*?## .*$$" $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'; echo "";\

.PHONY: sonar-scanner
sonar-scanner: ## run sonarqube-scanner for all supported tools
	SONAR_TOKEN=$(SONAR_TOKEN) RELEASE_VERSION=$(RELEASE_VERSION) ./sonar-scanner.sh

.PHONY: sonar
sonar: ## run local sonar in docker
	docker-compose --file "$(CURDIR)"/sonar/docker-compose.yml up --detach

.PHONY: kowlma
kowl: ## run kowl on localhost:8080 via docker-compose
	docker-compose -f $(CURDIR)/../go/topkapi/docker-compose-kowl.yml up

.PHONY: install
install: ## install external tools that make our life easier (brew)
	for brewtool in $(BREW_TOOLS); do brew install $$brewtool; done

.PHONY: upgrade
upgrade: ## upgrade external tools that make our life easier (brew)
	for brewtool in $(BREW_TOOLS); do brew upgrade $$brewtool; done
	@echo "Finished for our core tools managed by brew, to get a full outdated list run 'brew outdated'"
	@echo "Or you can run target 'test' to test all tools"

.PHONY: dependa-list
dependa-list: ## list dependabot branches ready to merge
	./dependa-merge.sh -l

#awscli git go golangci-lint hadolint jq postgresql@12 tfswitch trivy yarn yq
test: ## test tools and show versions
	@aws --version
	@git --version
	@go version
	@golangci-lint --version
	@hadolint --version
	@jq --version
	@pg_ctl --version
	@echo "tfswitch $(shell tfswitch --version)" 
	@echo "trivy $(shell trivy --version|grep -E ^Version)"
	@echo "yarn $(shell yarn --version)"
	@yq --version

.PHONY: clean
clean: ## clean artifacts
	rm -f ../angkor.tgz

.PHONY: archive
archive: clean ## create angkor.tgz in root directory with all files except node_modules etc.
	cd .. && tar --exclude='./angular/node_modules' --exclude='./kotlin/.gradle' \
	--exclude='./terraform/.terraform/providers' --exclude='./kotlin/build' \
	-zcvf ./angkor.tgz .

# https://grafana.com/docs/grafana/latest/setup-grafana/installation/docker/
.PHONY: grafana
grafana: ## run grafana locally on port 3000 with docker
	@docker rm -f grafana || echo grafana not running
	mkdir -p grafana-data/
	@echo "Starting grafana in the background on http://localhost:3000"
	docker run -d -p 3000:3000 --name=grafana \
		-e "GF_LOG_LEVEL=debug" \
		--user "$(id -u)" \
  		--volume "$$PWD/grafana-data:/var/lib/grafana" \
 	 grafana/grafana-oss 

# https://squaredup.com/blog/three-ways-to-run-prometheus/
# https://prometheus.io/docs/prometheus/latest/getting_started/
# Remote writing only Prometheus consumes more than 10Gb of RAM:
# https://github.com/prometheus/prometheus/issues/5666
.PHONY: prom
prom: ## run prometheus locally on port 9090 with docker
	@docker ps -a -q -f name=prometheus && docker rm -f prometheus
	mkdir -p prometheus-data/
	@docker run -d -p 9090:9090 --name prometheus \
		--volume prometheus-data:/prometheus \
		--volume "$$PWD"/prometheus.yml:/etc/prometheus/prometheus.yml \
		prom/prometheus