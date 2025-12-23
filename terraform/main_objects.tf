###################################################
# manage local and remote remote files
###################################################

# store useful ENV vars in dotenv_content, then create local and remote version
locals {
  dotenv_content = templatefile("${path.module}/templates/.env_config", {
    account_id          = module.vpcinfo.account_id
    api_version         = var.api_version
    api_token_metrics   = module.tokens.api_token_metrics
    appid               = var.appid
    aws_region          = module.vpcinfo.aws_region
    bucket_name         = module.s3.bucket_name
    certbot_domain_name = var.certbot_domain_name
    certbot_domain_str = format("-d %s", join(" -d ", concat([
      var.certbot_domain_name], var.certbot_subject_alternative_names))
    )
    certbot_mail = var.certbot_mail
    # db_password              = var.db_password # move to HCP Secrets
    db_url             = var.db_url
    db_username        = var.db_username
    sonar_token        = module.secrets_read_ci.secrets["SONAR_TOKEN"]
    sonar_organization = var.sonar_organization

    container_registry           = var.container_registry
    container_registry_namespace = var.container_registry_namespace

    appctl_replica_db_url = module.secrets_read.secrets["APPCTL_REPLICA_DB_URL"]

    imprint_url         = var.imprint_url
    instance_id         = module.ec2.instance_id
    mapbox_access_token = var.mapbox_access_token
    tours_api_base_url  = var.tours_api_base_url
    tours_api_user_id   = var.tours_api_user_id
    photos_feed_url     = var.photos_feed_url
    oauth2_client_id    = module.cognito.app_client_id
    oauth2_client_name  = module.cognito.app_client_name
    # oauth2_client_secret     = module.cognito.app_client_secret # move to HCP Secrets
    oauth2_issuer_uri  = module.cognito.pool_issuer_uri
    oauth2_pool_domain = module.cognito.pool_domain
    # 2025-10-28 removed due to newly introduced AWS charges
    #oauth2_client_cli_id     = module.cognito.app_client_cli_id
    #oauth2_client_cli_secret = module.cognito.app_client_cli_secret

    public_ip = module.ec2.public_ip
    server_names = join(" ", concat([
      var.certbot_domain_name], var.certbot_subject_alternative_names)
    )
    ssh_privkey_file = pathexpand(var.ssh_privkey_file)
    ui_version       = var.ui_version
    smtp_user        = module.ses.mailer_access_key
    smtp_password    = module.ses.mailer_ses_smtp_password
    smtp_server      = module.ses.mailer_ses_smtp_server
    smtp_port        = module.ses.mailer_ses_smtp_port

    # remindabot
    remindabot_api_token = module.ec2.api_token # todo fully support HCP Secrets
    #remindabot_kafka_group_id = "${module.confluent.topic_acl_group_prefix}remindabot.prod"
    remindabot_kafka_group_id = "app.remindabot.prod" # todo: read "through" confluent module

    # appctl
    appctl_db_password = var.db_password # todo use dedicated backup password resp. HCP Secrets

    # Current Kafka setup @ Confluent
    kafka_rest_endpoint     = module.confluent.cluster_rest_endpoint
    kafka_bootstrap_servers = module.confluent.cluster_boostrap_servers
    kafka_cluster_id        = module.confluent.cluster_id

    # PHASE Secret Management
    phase_app_id = var.phase_app_id
    # phase_api_token = var.phase_api_token ## stored in ssm param store and pulled by appctl.sh

  })
  # appended for local purposes only (appended to local .env file, not remote S3 .env_config)
  dotenv_local_secrets = <<-EOT
# LOCAL SECRET SECTION
KAFKA_PRODUCER_API_KEY=${module.confluent.api_key_producer["dev"].id}
KAFKA_PRODUCER_API_SECRET=${module.confluent.api_key_producer["dev"].secret}
KAFKA_CONSUMER_API_KEY=${module.confluent.api_key_consumer["dev"].id}
KAFKA_CONSUMER_API_SECRET=${module.confluent.api_key_consumer["dev"].secret}
KAFKA_BOOTSTRAP_SERVERS=${module.confluent.cluster_boostrap_servers}
KAFKA_CLUSTER_ID=${module.confluent.cluster_id}
KAFKA_REST_ENDPOINT=${module.confluent.cluster_rest_endpoint}

EOT
}


# docker-compose.yml which handles everything managed by docker on ec2
resource "aws_s3_object" "docker_compose" {
  bucket        = module.s3.bucket_name
  key           = "deploy/docker-compose.yml"
  content       = file("${path.module}/files/docker-compose.yml")
  storage_class = "REDUCED_REDUNDANCY"
}

# appctl.sh is our main control script on ec2 for various tasks
resource "aws_s3_object" "deploy_script" {
  bucket        = module.s3.bucket_name
  key           = "deploy/appctl.sh"
  content       = file("${path.module}/files/appctl.sh")
  storage_class = "REDUCED_REDUNDANCY"
}

# local .env copy in ~/.angkor/.env for for dev purposes and parent Makefile
# note that the local dotenv file will also container dev secrets,
# on EC2 this will be handled by appctl.sh which pulls the secrets from HCP Vault Secrets
resource "local_file" "dotenv" {
  content         = join("", [local.dotenv_content, local.dotenv_local_secrets])
  file_permission = "0644"
  # pathexpand takes a filesystem path that might begin with a ~ segment, and if so it replaces that segment with the current user's home directory path.
  filename = pathexpand(var.local_dotenv_file) # e.g. ~/.angkor/.env
}

locals {
  kafbat_template_location = "${path.module}/templates/kafbat-config.yml"
}
resource "local_file" "kafbat_config" {
  content = templatefile(local.kafbat_template_location, {
    kafbat_template_location         = local.kafbat_template_location
    kafka_bootstrap_servers          = module.confluent.cluster_boostrap_servers
    kafka_cluster_manager_api_key    = module.confluent.api_key_cluster_manager.id
    kafka_cluster_manager_api_secret = module.confluent.api_key_cluster_manager.secret
  })
  file_permission = "0644"
  filename        = "${path.module}/../tools/kafbat-config.yml" // file(...) won't work if the file doesn't exist yet
}

# local tools/kafbat-config.yml file for kafbat UI tool


# remote s3 .env in /home/ec2user for the docker-compose and friends
resource "aws_s3_object" "dotenv" {
  bucket  = module.s3.bucket_name
  key     = "deploy/.env_config"
  content = local.dotenv_content
  # https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html#AmazonS3-PutObject-request-header-StorageClass
  storage_class = "STANDARD_IA" # "REDUCED_REDUNDANCY"
}

