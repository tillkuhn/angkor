[![Releases](https://img.shields.io/github/v/tag/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/releases)
[![License](https://img.shields.io/github/license/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/blob/master/LICENSE)
[![david-dm](https://david-dm.org/tillkuhn/angkor.svg?path=ui)](https://david-dm.org/tillkuhn/angkor?path=ui)
![kotlin-ci](https://github.com/tillkuhn/angkor/workflows/kotlin-ci/badge.svg)
![angular-ci](https://github.com/tillkuhn/angkor/workflows/angular-ci/badge.svg)

## AngKoR - Angular Kotlin RESTful Webapp Stack
![](docs/modules/ROOT/images/img_4075_angkor_sunrise_pano.jpg)

This (almost) purely educational application manages places I'd like to visit some day. 
Key technologies: [Angular](https://angular.io/) based single-page app with Mapbox GL, AWS Cognito for OAuth2, S3 and PostgreSQL DB for persistence and
a Spring Boot backend written in [Kotlin](https://kotlinlang.org/), 
all created on AWS Infrastructure with [Terraform](https://www.terraform.io/) and Confidence.

## tl;dr

```shell script
$ make angkor
🌇 Successfully Built Angkor 
```

## Infrastructure

You should have [AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and most importantly [Terraform](https://www.terraform.io/intro/getting-started/install.html) installed.
In a nutshell the application's neighborhood looks as follows (credits to [cloudcraft.co](https://cloudcraft.co/) for their nice web based drawing tool):

![](./docs/modules/ROOT/images/infrastructure.png)

## Angkor wasn't built in a day ... 

We use good old [GNU Make](https://www.gnu.org/software/make/) utility to manage all tasks for terraform, gradle, yarn
and whatever else we have in our ecosystem centrally. Rund without args to see what's possible, open the [Makefile](./Makefile) to look beyond!

```shell script
$ make
  api-clean            Cleans up ./api/build folder
  api-build            Assembles backend jar in ./api/build with gradle (alias: assemble)
  api-run              Runs springBoot API in ./api using gradle bootRun (alias: bootrun)
  api-dockerize        Builds API docker images on top of recent opdenjdk
  api-push             Build and tags API docker image, and pushes to dockerhub
  api-deploy           Deploys API with subsequent pull and restart of server on EC2

  ui-clean             Remove UI dist folder ./ui/dist
  ui-build             Run ng build  in ./ui
  ui-build-prod        Run ng build  in ./ui
  ui-run               Run UI with ng serve and opens UI in browser (alias: serve,open)
  ui-dockerize         Creates UI docker image based on nginx
  ui-push              Creates UI docker frontend image and deploys to dockerhub
  ui-deploy            Deploys UI with subsequent pull and restart of server on EC2
  ui-mocks             Run json-server on foreground to mock API services for UI (alias: mock)

  infra-init           Runs terraform init on working directory ./infra
  infra-plan           Runs terraform plan with implicit init and fmt (alias: plan)
  infra-deploy         Runs terraform apply with auto-approval (alias: apply)

  ec2-stop             Stops the ec2 instance (alias: stop)
  ec2-start            Launches the ec-2instamce (alias: start)
  ec2-status           Get ec2 instance status (alias: status)
  ec2-ps               Run docker compose status on instance (alias: ps)
  ec2-login            Exec ssh login into current instance (alias: ssh)
  ec2-pull             Pull recent config on server, triggers docker-compose up (alias: pull)

  docs-build           Generate documentation site usingantora-playbook.yml (alias: docs)
  docs-deploy          Generate documentation site tp s3

  all-clean            Clean up build artifact directories in backend and frontend (alias: clean)
  all-build            Builds frontend and backend (alias: build)
  all-deploy           builds and deploys frontend and backend images (alias deploy)

  angkor               The ultimate target - builds and deploys everything 🦄

```

## Anybody listening?

```shell script
$ curl -sS http://localhost:8080/actuator/health
{"status":"UP"}
$ open http://localhost:4200
```

## I want more Documentation

Seriously? Coming soon: Dedicated documentation project built with [Antora](https://antora.org/). 
You can already check out the [sources](./docs/modules/ROOT/pages), It's asciidoc, so it's easy to read w/o transformation.

## Contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md)
