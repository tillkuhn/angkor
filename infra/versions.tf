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
  required_version = ">= 0.14"
}
