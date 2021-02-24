terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      # check version https://registry.terraform.io/providers/hashicorp/aws/latest
      version = "~> 3.22.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "~> 1.4.0"
    }
    http = {
      version = "~> 1.2.0"
    }
  }
  # terraform itself. if patch version not specified, it will always use latest (e.g. 1.14.7 if >= 0.14)
  # make sure to align expected version with .terraform-version and github workflow 'infra'
  required_version = ">= 0.14"
}
