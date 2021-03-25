[![Releases](https://img.shields.io/github/v/tag/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/releases)
[![License](https://img.shields.io/github/license/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=angkor-api&metric=alert_status)](https://sonarcloud.io/dashboard?id=angkor-api)
![kotlin-ci](https://github.com/tillkuhn/angkor/workflows/kotlin-ci/badge.svg)
![angular-ci](https://github.com/tillkuhn/angkor/workflows/angular-ci/badge.svg) 
[![Go Report Card](https://goreportcard.com/badge/github.com/tillkuhn/angkor)](https://goreportcard.com/report/github.com/tillkuhn/angkor)

## AngKoR - Angular Kotlin RESTful Webapp Stack
![](docs/modules/ROOT/images/img_4075_angkor_sunrise_pano.jpg)

This (almost) purely educational app manages places I'd like to visit some day, and helps me to keep track of more or less exotic dishe recipes.  

Key technologies: [Angular](https://angular.io/) based single-page app with [Mapbox GL](https://docs.mapbox.com/mapbox-gl-js/api/), [AWS Cognito](https://aws.amazon.com/cognito/) for OAuth2, [PostgreSQL](https://www.postgresql.org/) DB and [S3](https://aws.amazon.com/s3/) for persistence and a [Spring Boot](https://spring.io/projects/spring-boot) backend written in [Kotlin](https://kotlinlang.org/), various spin-off tools written in [Golang](https://golang.org/), all provisioned to AWS Infrastructure with [Terraform](https://www.terraform.io/) and lots of Confidence.

## tl;dr

```shell script
$ make angkor
🌇 Successfully Built Angkor 
```

## Modules

Angkor is a *monorepo* which combines the following modules and technologies:

| Path   | Descriptions                         | Technologies / Tools / Language(s)                                                    | Build Status                                                                          |
|--------|--------------------------------------|---------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `/infra` | Cloud Infrastructure as Code         | Terraform, HCL, [aws](https://aws.amazon.com/)                      | ![ terraform-ci](https://github.com/tillkuhn/angkor/workflows/terraform-ci/badge.svg) |
| `/api`   | Server Backend                       | Kotlin, Spring Boot, Gradle, docker                                         | ![ kotlin-ci](https://github.com/tillkuhn/angkor/workflows/kotlin-ci/badge.svg)       |
| `/ui`    | Frontend and reverse proxy           | Angular, [TypeScript](https://www.typescriptlang.org/), yarn, nginx, docker | ![ angular-ci](https://github.com/tillkuhn/angkor/workflows/angular-ci/badge.svg)     |
| `/tools` | Supporting services such as webhooks | [golang](https://golang.org/), systemd                                       | ![ golang-ci](https://github.com/tillkuhn/angkor/workflows/golang-ci/badge.svg)       |
| `/docs`  | Project Documentation                | [Antora](https://antora.org/), asciidoc                             | ![ antora-ci](https://github.com/tillkuhn/angkor/workflows/antora-ci/badge.svg)       | 

## Components & Infrastructure

You should have [AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and most importantly [Terraform](https://www.terraform.io/intro/getting-started/install.html) installed.
In a nutshell the application's neighborhood looks as follows: 

![](https://timafe.files.wordpress.com/2020/12/angkor-components.png)

## Angkor wasn't built in a day ... 

We use good old [GNU Make](https://www.gnu.org/software/make/) utility to manage all tasks for terraform, gradle, yarn
and whatever else we have in our ecosystem centrally. Rund without args to see what's possible, open the [Makefile](./Makefile) to look beyond!

```shell script
  $ make
  api-clean            Cleans up ./api/build folder
  api-build            Assembles backend jar in ./api/build with gradle (alias: assemble)
  api-test             Runs spring boot unit and integration tests in ./api
  api-run              Runs springBoot API in ./api using gradle bootRun (alias: bootrun)
  api-mock             Runs OIDC (and potentially other) mock service for api
  api-deploy           Deploys API with subsequent pull and restart of server on EC2

  ui-clean             Remove UI dist folder ./ui/dist
  ui-build             Run ng build  in ./ui
  ui-build-prod        Run ng build --prod in ./ui
  ui-test              Runs chromeHeadless tests in ./ui
  ui-run               Run UI with ng serve and opens UI in browser (alias: serve,open,ui)
  ui-deploy            Deploys UI with subsequent pull and restart of server on EC2
  ui-mocks             Run json-server on foreground to mock API services for UI (alias: mock)

  infra-init           Runs terraform init on working directory ./infra, switch to
  infra-plan           Runs terraform plan with implicit init and fmt (alias: plan)
  infra-deploy         Runs terraform apply with auto-approval (alias: apply)

  ec2-stop             Stops the ec2 instance (alias: stop)
  ec2-start            Launches the ec-2instamce (alias: start)
  ec2-status           Get ec2 instance status (alias: status)
  ec2-ps               Run docker compose status on instance (alias: ps)
  ec2-login            Exec ssh login into current instance (alias: ssh,login)
  ec2-deploy           Pull recent config on server, triggers docker-compose up (alias: pull)

  docs-clean           Cleanup docs build directory
  docs-build           Generate documentation site using antora-playbook.yml
  docs-push            Generate documentation site and push to s3
  docs-deploy          Deploys docs with subsequent pull and restart of server on EC2 (alias: docs)

  tools-test           Run lint and tests (tbi)
  tools-deploy         Interim task to trigger re-init of tools on server side

  all-clean            Clean up build artifact directories in backend and frontend (alias: clean)
  all-build            Builds frontend and backend (alias: build)
  all-test             Builds frontend and backend (alias: build)
  all-deploy           builds and deploys frontend and backend images (alias deploy)

  angkor               The ultimate target - builds and deploys everything 🦄

  release              create final release tag with semtag
```

## I want more Documentation

Seriously? Check our Dedicated **[angkor-docs](https://dev.timafe.net/angkor-docs/angkor-docs/)**  project built with [Antora](https://antora.org/)


## Contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md)
