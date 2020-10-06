## Main Entry point for terraform infrastructure
provider "aws" {
  region = "eu-central-1"
}

## A local value assigns a name to an expression, allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags = map("appid", var.appid, "managedBy", "terraform")
}

## see terraform-backend.tf.tmpl and remove extension
## to enable s3 backend for remote shared terraform state

module "vpcinfo" {
  source = "./modules/vpcinfo"
}

module "s3" {
  source        = "./modules/s3"
  appid         = var.appid
  tags          = local.common_tags
  aws_region    = var.aws_region
  aws_s3_prefix = var.aws_s3_prefix
}

## setup messaging
module "messaging" {
  source        = "./modules/messaging"
  appid         = var.appid
  bucket_arn    = module.s3.bucket_arn
  delay_seconds = "0" # so all actions are most likely to be finished, since notifcation is currently only set by tools
  tags          = local.common_tags
}


module "iam" {
  source      = "./modules/iam"
  appid       = var.appid
  tags        = local.common_tags
  bucket_name = module.s3.bucket_name
  topic_arn   = module.messaging.topic_arn
  queue_arn   = module.messaging.queue_arn
}

module "ec2" {
  source          = "./modules/ec2"
  appid           = var.appid
  tags            = local.common_tags
  aws_subnet_name = var.aws_subnet_name
  aws_vpc_name    = var.aws_vpc_name
  ssh_pubkey_file = pathexpand(var.ssh_pubkey_file)
  user_data = templatefile("${path.module}/templates/user-data.sh", {
    appid       = var.appid
    bucket_name = aws_s3_bucket_object.dockercompose.bucket
    # a bit ugly since the script with
    certbot_domain_str = format("-d %s", join(" -d ", concat([var.certbot_domain_name], var.certbot_subject_alterntive_names)))
    certbot_mail       = var.certbot_mail
  })
  instance_profile_name = module.iam.instance_profile_name
}

# Let's talk about DNS
module "route53" {
  source                    = "./modules/route53"
  domain_name               = var.certbot_domain_name
  subject_alternative_names = var.certbot_subject_alterntive_names
  hosted_zone_id            = var.hosted_zone_id
  public_ip                 = module.ec2.instance.public_ip
}

# Cognito User Pool for OAuth2 and social media login
module "cognito" {
  source                    = "./modules/cognito"
  appid                     = var.appid
  callback_urls             = var.cognito_callback_urls
  fb_provider_client_id     = var.cognito_fb_provider_client_id
  fb_provider_client_secret = var.cognito_fb_provider_client_secret
  app_client_name           = var.cognito_app_client_name
  auth_domain_prefix        = var.cognito_auth_domain_prefix
  tags                      = local.common_tags
}

## setup deployment user for github actions
module "param" {
  source = "./modules/param"
  appid  = var.appid
  key    = "docker_token"
  #bucket_path = "deploy"
  value     = var.docker_token
  upper_key = true
  tags      = local.common_tags
}

## setup deployment user for github actions
module "deploy" {
  source      = "./modules/deploy"
  appid       = var.appid
  bucket_name = module.s3.bucket_name
  topic_arn   = module.messaging.topic_arn
  #bucket_path = "deploy"
  tags = local.common_tags
}
