![Angular](https://img.shields.io/badge/angular-%23DD0031.svg?style=for-the-badge&logo=angular&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Go](https://img.shields.io/badge/go-%2300ADD8.svg?style=for-the-badge&logo=go&logoColor=white)
![Terraform](https://img.shields.io/badge/terraform-%235835CC.svg?style=for-the-badge&logo=terraform&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

[![Releases](https://img.shields.io/github/v/tag/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/releases)
![GitHub language count](https://img.shields.io/github/languages/count/tillkuhn/angkor)
[![License](https://img.shields.io/github/license/tillkuhn/angkor?color=blue)](https://github.com/tillkuhn/angkor/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=angkor-api&metric=alert_status)](https://sonarcloud.io/dashboard?id=angkor-api)
![kotlin-ci](https://github.com/tillkuhn/angkor/workflows/kotlin-ci/badge.svg)
![angular-ci](https://github.com/tillkuhn/angkor/workflows/angular-ci/badge.svg) 
[![Go Report Card](https://goreportcard.com/badge/github.com/tillkuhn/angkor)](https://goreportcard.com/report/github.com/tillkuhn/angkor)

[//]: # (COOL: dynamic TOML Badge https://shields.io/badges/dynamic-toml-badge)
![Dynamic TOML Badge](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ftillkuhn%2Fangkor%2Fmain%2Fkotlin%2Fgradle%2Flibs.versions.toml&query=%24.versions%5B'spring-boot'%5D&label=spring-boot&color=green)

[//]: # ( https://img.shields.io/keybase/pgp/tillkuhn )
[//]: # (check https://github.com/Naereen/badges and https://github.com/Ileriayo/markdown-badges for more interesting badges)

## Project "Angkor" - Angular Golang Kotlin RESTful Webapp Stack
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
| `/terraform` | Cloud Infrastructure as Code         | ![Terraform](https://img.shields.io/badge/terraform-%235835CC.svg?style=for-the-badge&logo=terraform&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black) | ![ terraform-ci](https://github.com/tillkuhn/angkor/workflows/terraform-ci/badge.svg) |
| `/kotlin`   | Server Backend                       | ![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white), ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white) ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)                                         | ![ kotlin-ci](https://github.com/tillkuhn/angkor/workflows/kotlin-ci/badge.svg)       |
| `/angular`    | Frontend and reverse proxy           | ![Angular](https://img.shields.io/badge/angular-%23DD0031.svg?style=for-the-badge&logo=angular&logoColor=white) ![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white) ![Yarn](https://img.shields.io/badge/yarn-%232C8EBB.svg?style=for-the-badge&logo=yarn&logoColor=white) ![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white) ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) | ![ angular-ci](https://github.com/tillkuhn/angkor/workflows/angular-ci/badge.svg)     |
| `/go` | Supporting services written in Go | ![Go](https://img.shields.io/badge/go-%2300ADD8.svg?style=for-the-badge&logo=go&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)| ![ golang-ci](https://github.com/tillkuhn/angkor/workflows/golang-ci/badge.svg)       |
| `/docs`  | Project Documentation                | [Antora](https://antora.org/), [AsciiDoc](https://asciidoc-py.github.io/)                             | ![ antora-ci](https://github.com/tillkuhn/angkor/workflows/antora-ci/badge.svg)       | 

## Components & Infrastructure

You should have [AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and most importantly [Terraform](https://www.terraform.io/intro/getting-started/install.html) installed.
In a nutshell the application's neighborhood looks as follows: 

![](https://timafe.files.wordpress.com/2021/05/anchorarch5.png)

## Impressions

### Places to Go (Details)
![](docs/modules/ROOT/images/preview_places.jpg)

### WorldWideMap (Overview)
![](docs/modules/ROOT/images/preview_map.jpg)

### Wish a Dish (Search)
![](docs/modules/ROOT/images/preview_dishes.jpg)

## Angkor wasn't built in a day ...

This project uses the good old [GNU Make](https://www.gnu.org/software/make/) utility to manage all tasks for terraform, gradle, yarn and whatever else we have in our ecosystem. Run `make` without args to see what's possible, open the [Makefile](./Makefile) to look beyond!

```shell script
$ make

  █████╗ ███╗   ██╗ ██████╗ ██╗  ██╗ ██████╗ ██████╗
 ██╔══██╗████╗  ██║██╔════╝ ██║ ██╔╝██╔═══██╗██╔══██╗
 ███████║██╔██╗ ██║██║  ███╗█████╔╝ ██║   ██║██████╔╝
 ██╔══██║██║╚██╗██║██║   ██║██╔═██╗ ██║   ██║██╔══██╗
 ██║  ██║██║ ╚████║╚██████╔╝██║  ██╗╚██████╔╝██║  ██║
 ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝

Available Commands:
  api-clean            Cleans up ./kotlin/build folder
  api-build            Assembles backend jar in ./api/build with gradle (alias: assemble)
  api-test             Runs spring boot unit and integration tests in ./kotlin
  api-run              Runs springBoot API in ./kotlin using gradle bootRun (alias: bootrun)
  api-mock             Runs OIDC (and potentially other) mock service for api

  ui-build             Run ng build  in ./ui
  ui-build-prod        Run ng build --prod in ./ui
  ui-test              Runs chromeHeadless tests in ./angular
  ui-run               Run angular with ng serve and opens WebUI in browser (alias: serve,open,angular)
  ui-mocks             Run json-server on foreground to mock API services for UI (alias: mock)

  tf-init              Runs terraform init on working directory ./terraform, switch to
  tf-plan              Runs terraform plan with implicit init and fmt (alias: plan)
  tf-apply             Runs terraform apply with auto-approval (alias: apply)


  docs-clean           Cleanup docs build directory
  docs-build           Generate documentation site using antora-playbook.yml
  docs-push            Generate documentation site and push to s3
  docs-deploy          Deploys docs with subsequent pull and restart of server on EC2 (alias: docs)


  all-clean            Clean up build artifact directories in backend and frontend (alias: clean)
  all-build            Builds frontend and backend (alias: build)
  all-test             Builds frontend and backend (alias: build)
  all-deploy           builds and deploys frontend and backend images (alias deploy)

  angular-clean        Remove angular dist folder ./angular/dist
  angkor               The ultimate target - builds and deploys everything 🦄

  release              create final release tag with semtag

  git-clean            git cleanup, e.g. delete up stale git branches
```

## I want more Documentation

Seriously? Check our Dedicated **[angkor-docs](https://dev.timafe.net/angkor-docs/angkor-docs/)**  project built with [Antora](https://antora.org/)


## Contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md)
