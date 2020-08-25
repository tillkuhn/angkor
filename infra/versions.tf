terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.3.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "~> 1.4.0"
    }
    http = {
      version = "~> 1.2.0"
    }
  }
  required_version = ">= 0.13"
}
