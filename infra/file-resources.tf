# remote files
resource "aws_s3_bucket_object" "dockercompose" {
  bucket = module.s3.bucket_name
  key    = "deploy/docker-compose.yml"
  content = templatefile("${path.module}/templates/docker-compose.yml", {
    appid                = var.appid
    db_url               = var.db_url
    db_username          = var.db_username
    db_password          = var.db_password
    api_version          = var.api_version
    ui_version           = var.ui_version
    docker_user          = var.docker_user
    certbot_domain_name  = var.certbot_domain_name
    mapbox_access_token  = var.mapbox_access_token
    oauth2_client_id     = module.cognito.app_client_id
    oauth2_client_name   = module.cognito.app_client_name
    oauth2_client_secret = module.cognito.app_client_secret
    oauth2_issuer_uri    = module.cognito.pool_issuer_uri
    server_names = join(" ", concat([
    var.certbot_domain_name], var.certbot_subject_alterntive_names))
  })
  storage_class = "REDUCED_REDUNDANCY"
}

## convert files first to substitute variables
resource "aws_s3_bucket_object" "deployscript" {
  bucket        = module.s3.bucket_name
  key           = "deploy/appctl.sh"
  content       = file("${path.module}/files/appctl.sh")
  storage_class = "REDUCED_REDUNDANCY"
}

## simple webhook http listener
resource "aws_s3_bucket_object" "webhook" {
  bucket = module.s3.bucket_name
  key    = "deploy/captain-hook.py"
  content = templatefile("${path.module}/templates/captain-hook.py", {
    certbot_domain_name = var.certbot_domain_name
  })
  storage_class = "REDUCED_REDUNDANCY"
}

locals {
  dotenv_content = templatefile("${path.module}/templates/.env", {
    appid               = var.appid
    ssh_privkey_file    = pathexpand(var.ssh_privkey_file)
    bucket_name         = module.s3.bucket_name
    instance_id         = module.ec2.instance.id
    public_ip           = module.ec2.instance.public_ip
    db_url              = var.db_url
    db_username         = var.db_username
    db_password         = var.db_password
    api_version         = var.api_version
    ui_version          = var.ui_version
    docker_token        = var.docker_token
    docker_user         = var.docker_user
    certbot_mail        = var.certbot_mail
    certbot_domain_name = var.certbot_domain_name
    certbot_domain_str = format("-d %s", join(" -d ", concat([
    var.certbot_domain_name], var.certbot_subject_alterntive_names)))
  })
}

# local files
resource "local_file" "dotenv" {
  content  = local.dotenv_content
  filename = "${path.module}/.env"
}

# remote version
resource "aws_s3_bucket_object" "dotenv" {
  bucket        = module.s3.bucket_name
  key           = "deploy/.env"
  content       = local.dotenv_content
  storage_class = "REDUCED_REDUNDANCY"
}
