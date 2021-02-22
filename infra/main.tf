###################################################
# Main Entry point for our terraform infrastructure
###################################################
provider "aws" {
  region = "eu-central-1"
}
# see terraform-backend.tf.tmpl and remove extension
# to enable s3 backend for remote shared terraform state


# terraform apply  -target=module.release
# terraform output -raw release
module "release" {
  source = "./modules/release"
  id     = var.release
  appid  = var.appid
  tags   = map("managedBy", "terraform")
}

# A local value assigns a name to an expression,
# allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags = map(
    "appid", var.appid,
    "managedBy", "terraform",
    "releaseName", module.release.name,
    "releaseVersion", module.release.version
  )
}

# collect useful aws vpc data from current context
module "vpcinfo" {
  source = "./modules/vpcinfo"
}

# manage data bucket(s) for prod and dev
module "s3" {
  source        = "./modules/s3"
  appid         = var.appid
  tags          = local.common_tags
  aws_region    = var.aws_region
  aws_s3_prefix = var.aws_s3_prefix
  dev_suffix    = var.dev_suffix
  expiry_prefix = "backup/db/history"
}


# setup sns/sqs messaging including dev queue
module "messaging" {
  source = "./modules/messaging"
  //for_each      = toset(["${var.appid}-events", "${var.appid}-events-dev"])
  for_each = {
    prod = "${var.appid}-events"
    dev  = "${var.appid}-events-dev"
  }
  name       = each.value
  bucket_arn = module.s3.bucket_arn
  tags       = local.common_tags
}

# manage IAM permissions, e.g. for ec2 instance profile
module "iam" {
  source      = "./modules/iam"
  appid       = var.appid
  tags        = local.common_tags
  bucket_name = module.s3.bucket_name
  topic_arn   = module.messaging["prod"].topic_arn
  queue_arn   = module.messaging["prod"].queue_arn
}

# manage ec2 instance, give us some compute power
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
    certbot_domain_str = format("-d %s", join(" -d ", concat([
    var.certbot_domain_name], var.certbot_subject_alterntive_names)))
    certbot_mail = var.certbot_mail
  })
  instance_profile_name = module.iam.instance_profile_name
}

# Let's talk about DNS
module "route53" {
  source                    = "./modules/route53"
  domain_name               = var.certbot_domain_name
  subject_alternative_names = var.certbot_subject_alterntive_names
  hosted_zone_id            = var.hosted_zone_id
  public_ip                 = module.ec2.public_ip
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

# Setup deployment user for github actions
module "param" {
  source = "./modules/param"
  for_each = {
    docker_token        = var.docker_token
    mapbox_access_token = var.mapbox_access_token
  }
  key       = each.key
  value     = each.value
  appid     = var.appid
  upper_key = true
  tags      = local.common_tags
}

# Setup deployment user for github actions
module "deploy" {
  source      = "./modules/deploy"
  appid       = var.appid
  bucket_name = module.s3.bucket_name
  topic_arn   = module.messaging["prod"].topic_arn
  #bucket_path = "deploy"
  tags = local.common_tags
}

# Setup ses for mail deliver
module "ses" {
  source = "./modules/ses"
  appid  = var.appid
  // pgp_key = base64encode(file(var.ssh_pubkey_file))
  tags           = local.common_tags
  domain_name    = var.certbot_domain_name
  hosted_zone_id = var.hosted_zone_id
}
