# common
variable "aws_region" {
  default = "eu-central-1"
}

variable "appid" {
  description = "Application ID"
}

variable "app_slug" {
  description = "Application Slug"
}

variable "dev_suffix" {
  default     = "dev"
  description = "suffix for additional dev resources"
}

# ec2
variable "aws_vpc_name" {
  description = "Name tag of your vpc"
}

variable "aws_subnet_name" {
  description = "Name tag of your subnet"
}

variable "aws_instance_type" {
  description = "type of the EC2 instance"
  default     = "t3a.nano"
}

# local file locations.
# use  pathexpand function (e.g. pathexpand(var.ssh_privkey_file)) if you work with ~home
variable "ssh_pubkey_file" {
  description = "location of your public key which will be used for keypair, may contain ~"
  default     = "~/.angkor/angkor.pem.pub"
}
# value won't be part of state, we just need location for scripts
variable "ssh_privkey_file" {
  description = "location of your privkey whose value will stay local (only for scripting), may contain ~."
  default     = "~/.angkor/angkor.pem"
}
variable "local_dotenv_file" {
  description = "location of .env to output dynamic tf resources to be used for development , may contain ~"
  default     = "~/.angkor/.env"
}

# local dev
variable "sonar_token" {
  description = "sonar login token for sonarcloud.io"
}

variable "sonar_login" {
  default     = "admin"
  description = "login to local sonar instance for analysis upload, can be also a token"
}

variable "sonar_password" {
  default     = ""
  description = "login password to local sonar instance for analysis upload, leave empty if login == token"
}

# Route53
variable "hosted_zone_id" {
  type        = string
  description = "hosted zone to create record"
}

variable "certbot_domain_name" {
  type        = string
  description = "fully qualified domain name, if alternatives are used this should be the one w/o host e.g. my-domain.com"
}

variable "certbot_subject_alternative_names" {
  type        = list(string)
  default     = []
  description = "subject alternative names e.g. host1.my-domain.com host2.my-domain.com"
}

variable "certbot_mail" {
  type        = string
  description = "mail address to be used as certbot contact"
}

# s3
variable "aws_s3_prefix" {
  type        = string
  description = "Prefix for s3 buckets to make them unique e.g. domain"
}

# app secrets
variable "db_url" {}
variable "db_username" {}
variable "db_password" {}
variable "db_api_key" {
  default     = "" # not required
  description = "Key to use https://www.elephantsql.com/docs/api.html"
}

# docker tags so we control which "version" to pull
variable "api_version" {}
variable "ui_version" {}

# docker hub now managed by HCP Vault ci-secrets-manual "app"
#variable "docker_token" {}
#variable "docker_user" {}

# mapbox api token
variable "mapbox_access_token" {}

# Base url for external API and User Id to retrieve tour info
variable "tours_api_base_url" {}
variable "tours_api_user_id" {}

# RSS Feed URL for tours, optional
variable "photos_feed_url" {
  default = ""
}

# custom impress url aka imprint
variable "imprint_url" {}

# Cognito config for OAuth2 / OIDC
variable "cognito_callback_urls" {}
variable "cognito_fb_provider_client_secret" {}
variable "cognito_fb_provider_client_id" {}
variable "cognito_google_provider_client_secret" {}
variable "cognito_google_provider_client_id" {}
variable "cognito_app_client_name" {}
variable "cognito_auth_domain_prefix" {}

variable "release" {
  description = "should be stored in release.auto.tfvars (and not versioned)"
}

# Kafka Topics Support (CloudKarafka)
variable "kafka_brokers" { description = "comma separate list of brokers in host:port format" }
variable "kafka_sasl_username" { description = "SASL Authentication Username" }
variable "kafka_sasl_password" { description = "SASL Authentication Password" }
variable "kafka_topic_prefix" { description = "Optional prefix that will be auto-prepended to all topics" }

# Kafka Topics Support (Confluent)
variable "confluent_cloud_api_key" {
  description = "Cloud API Key with organizational privileges (need to create the initial environment)"
}
variable "confluent_cloud_api_secret" {
  description = "Corresponding Cloud API Secret"
}

variable "hcp_client_id" {}
variable "hcp_client_secret" {}
