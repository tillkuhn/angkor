## Main Entry point for terraform infrastructure
provider "aws" {
  region = "eu-central-1"
  version = "~> 2.66"
}

## A local value assigns a name to an expression, allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags = map("appid", var.appid, "managedBy", "terraform")
}

## see terraform-backend.tf.tmpl and remove extension
## to enable s3 backend for remote shared terraform state

## locate existing vpc by name
data "aws_vpc" "vpc" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_vpc_name]
  }
}

output "hello" {
  value = "hello terraform"
}
