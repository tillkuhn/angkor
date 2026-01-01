###################################################
# Main Entry point for our terraform infrastructure
###################################################

# A local value assigns a name to an expression,
# allowing it to be used multiple times within a module without repeating it.
locals {
  common_tags = tomap({
    "appid"          = var.appid,
    "managedBy"      = "terraform",
    "releaseName"    = module.release.name,
    "releaseVersion" = module.release.version
    }
  )
}

# terraform apply  -target=module.release
# terraform output -raw release
module "release" {
  source = "./modules/release"
  id     = var.release
  appid  = var.appid
  tags   = tomap({ "managedBy" = "terraform" })
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
    bucket_name = aws_s3_object.docker_compose.bucket
    certbot_domain_str = format("-d %s", join(" -d ", concat([
    var.certbot_domain_name], var.certbot_subject_alternative_names)))
    certbot_mail = var.certbot_mail
  })
  instance_profile_name = module.iam.instance_profile_name
}

# Let's talk about DNS
module "route53" {
  source                    = "./modules/route53"
  domain_name               = var.certbot_domain_name
  subject_alternative_names = var.certbot_subject_alternative_names
  hosted_zone_id            = var.hosted_zone_id
  public_ip                 = module.ec2.public_ip
}

# Cognito User Pool for OAuth2 and social media login
# This module can usually be also run in isolation, e.g.
# tofu apply  -target module.cognito
module "cognito" {
  source                        = "./modules/cognito"
  appid                         = var.appid
  callback_urls                 = var.cognito_callback_urls
  fb_provider_client_id         = var.cognito_fb_provider_client_id
  fb_provider_client_secret     = var.cognito_fb_provider_client_secret
  google_provider_client_id     = var.cognito_google_provider_client_id
  google_provider_client_secret = var.cognito_google_provider_client_secret
  app_client_name               = var.cognito_app_client_name
  auth_domain_prefix            = var.cognito_auth_domain_prefix
  tags                          = local.common_tags
}


locals {
  cluster_endpoint_no_protocol = trimprefix(module.confluent.cluster_rest_endpoint, "https://")
  ci_kafka_topic               = "ci.events"
}

# SSM Params ... use only for configuration  *not* covered by Phase secrets,
# with the exception of the initial api key(s) to access Phase from EC2 and CI, for example
# PHASE_APP_ID=$(aws ssm get-parameter --name /angkor/prod/PHASE_APP_ID  --with-decryption --query "Parameter.Value" --output text)
# PHASE_API_TOKEN=$(aws ssm get-parameter --name /angkor/prod/PHASE_API_TOKEN  --with-decryption --query "Parameter.Value" --output text)
# curl -fsSGH "Authorization: Bearer ServiceAccount $PHASE_API_TOKEN" "https://api.phase.dev/v1/secrets/" \
#      -d app_id=${PHASE_APP_ID} -d env=development -d path=/tfwrite | \
#      jq -r '.[] | "\(.key)=\(.value)"'
#
module "param" {
  source = "./modules/param"
  for_each = {
    mapbox_access_token = var.mapbox_access_token # todo migrate to phase
    # sonar_token         = var.sonar_token         # todo migrate to phase
    phase_app_id    = var.phase_app_id    # required so the ec2 instance can access phase to retrieve further secrets
    phase_api_token = var.phase_api_token # also required for phase api interaction
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
  tags        = local.common_tags
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

# Tag based Resource Group for reporting
resource "aws_resourcegroups_group" "main" {
  name = "${var.appid}-group"
  resource_query {
    query = <<JSON
{
  "ResourceTypeFilters": [
    "AWS::AllSupported"
  ],
  "TagFilters": [
    {
      "Key": "appid",
      "Values": ["${var.appid}"]
    }
  ]
}
JSON
  }
}


# Setup Confluent Cloud
module "confluent" {
  source           = "./modules/confluent"
  app_id           = var.appid
  env_id           = "default"
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
  # list of kafka topics to create
  topics = [
    {
      name             = local.ci_kafka_topic # ci.events
      retention_hours  = 24 * 3
      partitions_count = 1
    },
    {
      name             = "app.events"
      retention_hours  = 24 * 91
      partitions_count = 1
    },
    {
      name             = "audit.events"
      retention_hours  = 24 * 365
      partitions_count = 1
    },
    {
      name             = "system.events"
      retention_hours  = 24 * 7
      partitions_count = 1
    },
    # dedicated cloud event target for development environment
    {
      name             = "dev.events"
      retention_hours  = 24 * 5
      partitions_count = 1
    },
    # play around topic for everybody, e.g. for connectivity tests
    {
      name             = "public.hello"
      retention_hours  = 24 * 2
      partitions_count = 1
    }
  ]
  service_accounts_producer = {
    app = {
      acl_prefixes = ["app.", "audit.", "system.", "public."]
    }
    ci = {
      acl_prefixes = ["ci.", "public."]
    }
    dev = {
      name         = "dev-producer" # default
      acl_prefixes = ["dev.", "public."]
    }
    system = {
      acl_prefixes = ["system.", "public."]
    }
  }
  service_accounts_consumer = {
    app = {
      acl_prefixes = ["app.", "audit.", "ci.", "system.", "public."]
    }
    dev = {
      name         = "dev-consumer" # default
      acl_prefixes = ["app.", "audit.", "ci.", "dev.", "public."]
    }
    # ci has no need to consume anything
    system = {
      acl_prefixes = ["ci.", "system.", "public."]
    }
  }
}

module "secrets_write_dev" {
  source = "./modules/secrets_write"
  app_id = var.phase_app_id
  env    = "development"
  secrets = [
    {
      name  = "dev_kafka_producer_api_key"
      value = module.confluent.api_key_producer["dev"].id
    },
    {
      name  = "dev_kafka_producer_api_secret"
      value = module.confluent.api_key_producer["dev"].secret
    },
    {
      name  = "dev_kafka_consumer_api_key"
      value = module.confluent.api_key_consumer["dev"].id
    },
    {
      name  = "dev_kafka_consumer_api_secret"
      value = module.confluent.api_key_consumer["dev"].secret
    },
  ]
}

# Write secrets to Phase
module "secrets_write_prod" {
  source    = "./modules/secrets_write"
  app_id    = var.phase_app_id
  env       = "production"
  upper_key = true
  secrets = [
    {
      name  = "oauth2_client_secret"
      value = module.cognito.app_client_secret
    },
    {
      name  = "db_password"
      value = var.db_password
    },
    {
      name  = "app_api_token"
      value = module.ec2.api_token
    },
    {
      name  = "grafana_viewer_key"
      value = module.grafana.service_account_token_viewer_key
    },
    {
      name  = "kafka_bootstrap_servers"
      value = module.confluent.cluster_boostrap_servers
    },
    {
      name  = "kafka_rest_endpoint"
      value = module.confluent.cluster_rest_endpoint
    },
    {
      name  = "kafka_cluster_id"
      value = module.confluent.cluster_id
    },
    {
      name  = "app_kafka_producer_api_key"
      value = module.confluent.api_key_producer["app"].id
    },
    {
      name  = "app_kafka_producer_api_secret"
      value = module.confluent.api_key_producer["app"].secret
    },
    {
      name  = "app_kafka_consumer_api_key"
      value = module.confluent.api_key_consumer["app"].id
    },
    {
      name  = "app_kafka_consumer_api_secret"
      value = module.confluent.api_key_consumer["app"].secret
    },
    # use system api producer for appctl
    {
      name  = "system_kafka_producer_api_key"
      value = module.confluent.api_key_producer["system"].id
    },
    {
      name  = "system_kafka_producer_api_secret"
      value = module.confluent.api_key_producer["system"].secret
    },
    {
      name  = "release_name"
      value = module.release.name
    },
    {
      name  = "release_version"
      value = module.release.version
    },
  ]
}


module "secrets_write_ci" {
  source = "./modules/secrets_write"
  app_id = var.phase_app_id
  env    = "staging"
  secrets = [
    # merge former ci secrets
    {
      name  = "kafka_producer_topic_url"
      value = "https://${module.confluent.api_key_producer["ci"].id}@${local.cluster_endpoint_no_protocol}/kafka/v3/clusters/${module.confluent.cluster_id}/topics/${local.ci_kafka_topic}"
    },
    {
      name  = "kafka_producer_api_secret"
      value = module.confluent.api_key_producer["ci"].secret
    },
    {
      name  = "release_name"
      value = module.release.name
    },
    {
      name  = "release_version"
      value = module.release.version
    },
  ]
}

module "secrets_read_ci" {
  source = "./modules/secrets_read"
  env    = "staging"
  app_id = var.phase_app_id
  #path  use default
}

module "secrets_read" {
  source = "./modules/secrets_read"
  env    = "production"
  app_id = var.phase_app_id
  #path  use default
}

module "grafana" {
  source = "./modules/grafana"
  # todo don't inherit prefix from cognito_auth_domain_prefix
  slug = var.cognito_auth_domain_prefix
  url  = "https://${var.cognito_auth_domain_prefix}.grafana.net/"
  #auth          = data.hcp_vault_secrets_app.rt_secrets_manual.secrets["GRAFANA_SA_TOKEN"]
  #cloud_api_key = data.hcp_vault_secrets_app.rt_secrets_manual.secrets["GRAFANA_CLOUD_API_KEY"]
  auth          = module.secrets_read.secrets["GRAFANA_SA_TOKEN"]
  cloud_api_key = module.secrets_read.secrets["GRAFANA_CLOUD_ACCESS_POLICY_TOKEN"] # formerly known as "GRAFANA_CLOUD_API_KEY"
}

module "tokens" {
  source = "./modules/tokens"
  app_id = var.appid
  keeper = module.ec2.api_token_keeper # formatdate("YYYY", timestamp()) # YYYY = rotate once a year
}
