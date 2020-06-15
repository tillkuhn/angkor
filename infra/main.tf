## Main Entry point for terraform infrastructure
provider "aws" {
  region  = "eu-central-1"
  version = "~> 2.66"
}

## A local value assigns a name to an expression, allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags      = map("appid", var.appid, "managedBy", "terraform")
  ssh_pubkey_file  = "../${var.appid}.pem.pub"
  ssh_privkey_file = "../${var.appid}.pem"
}

## see terraform-backend.tf.tmpl and remove extension
## to enable s3 backend for remote shared terraform state

module "s3" {
  source        = "./modules/s3"
  appid         = var.appid
  tags          = local.common_tags
  aws_region    = var.aws_region
  aws_s3_prefix = var.aws_s3_prefix
}

resource "aws_s3_bucket_object" "dockercompose" {
  bucket = module.s3.bucket_name
  key    = "deploy/docker-compose.yml"
  content = templatefile("${path.module}/templates/docker-compose.yml", {
    db_url      = var.db_url
    db_username = var.db_username
    db_password = var.db_password
    api_version = var.api_version
    ui_version  = var.ui_version
  })
  storage_class = "REDUCED_REDUNDANCY"
}


module "iam" {
  source      = "./modules/iam"
  appid       = var.appid
  bucket_name = module.s3.bucket_name
  tags        = local.common_tags

}

module "ec2" {
  source          = "./modules/ec2"
  appid           = var.appid
  aws_subnet_name = var.aws_subnet_name
  aws_vpc_name    = var.aws_vpc_name
  ssh_pubkey_file = local.ssh_pubkey_file
  user_data = templatefile("${path.module}/templates/user-data.sh", {
    appid               = var.appid
    bucket_name         = aws_s3_bucket_object.dockercompose.bucket
    certbot_domain_name = var.certbot_domain_name
    certbot_mail        = var.certbot_mail
  })
  instance_profile_name = module.iam.instance_profile_name
  tags                  = local.common_tags
}

module "route53" {
  source         = "./modules/route53"
  domain_name    = var.certbot_domain_name
  hosted_zone_id = var.hosted_zone_id
  public_ip      = module.ec2.instance.public_ip
}



## convert files first to substitute variables
resource "local_file" "env" {
  content = templatefile("${path.module}/templates/.env", {
    appid       = var.appid
    instance_id = module.ec2.instance.id
    public_ip   = module.ec2.instance.public_ip
    db_url      = var.db_url
    db_username = var.db_username
    db_password = var.db_password
    api_version = var.api_version
    ui_version  = var.ui_version
  })
  #content = "# ${var.appid} runtime variables\ninstance_id=${module.ec2.instance.id}\npublic_ip=${module.ec2.instance.public_ip}\n"
  #value = <<-EOT
  #hello
  #  world
  #EOT
  filename = "${path.module}/../.env"
}
