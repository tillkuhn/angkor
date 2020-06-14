## Main Entry point for terraform infrastructure
provider "aws" {
  region = "eu-central-1"
  version = "~> 2.66"
}

## A local value assigns a name to an expression, allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags = map("appid", var.appid, "managedBy", "terraform")
  ssh_pubkey_file = "../${var.appid}.pem.pub"
  ssh_privkey_file = "../${var.appid}.pem"
}

## see terraform-backend.tf.tmpl and remove extension
## to enable s3 backend for remote shared terraform state

module "ec2" {
  source = "./modules/ec2"
  appid = var.appid
  aws_subnet_name = var.aws_subnet_name
  aws_vpc_name = var.aws_vpc_name
  ssh_pubkey_file = local.ssh_pubkey_file
  tags = local.common_tags
}


## convert files first to substitute variables
resource "local_file" "setenv_sh" {
  content = "# ${var.appid} runtime variables\ninstance_id=${module.ec2.instance.id}\npublic_ip=${module.ec2.instance.public_ip}\n"
  filename = "${path.module}/local-env.sh"
}
