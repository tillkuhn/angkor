# remote files
resource "aws_s3_bucket_object" "dockercompose" {
  bucket        = module.s3.bucket_name
  key           = "deploy/docker-compose.yml"
  content       = file("${path.module}/files/docker-compose.yml")
  storage_class = "REDUCED_REDUNDANCY"
}

## convert files first to substitute variables
resource "aws_s3_bucket_object" "deployscript" {
  bucket        = module.s3.bucket_name
  key           = "deploy/appctl.sh"
  content       = file("${path.module}/files/appctl.sh")
  storage_class = "REDUCED_REDUNDANCY"
}

locals {
  dotenv_content = templatefile("${path.module}/templates/.env", {
    ACCOUNT_ID           = module.vpcinfo.account_id
    AWS_REGION           = module.vpcinfo.aws_region
    api_version          = var.api_version
    appid                = var.appid
    bucket_name          = module.s3.bucket_name
    certbot_domain_name  = var.certbot_domain_name
    certbot_domain_str   = format("-d %s", join(" -d ", concat([var.certbot_domain_name], var.certbot_subject_alterntive_names)))
    certbot_mail         = var.certbot_mail
    db_password          = var.db_password
    db_url               = var.db_url
    db_username          = var.db_username
    docker_token         = var.docker_token
    docker_user          = var.docker_user
    imprint_url          = var.imprint_url
    instance_id          = module.ec2.instance.id
    mapbox_access_token  = var.mapbox_access_token
    oauth2_client_id     = module.cognito.app_client_id
    oauth2_client_name   = module.cognito.app_client_name
    oauth2_client_secret = module.cognito.app_client_secret
    oauth2_issuer_uri    = module.cognito.pool_issuer_uri
    public_ip            = module.ec2.instance.public_ip
    server_names         = join(" ", concat([var.certbot_domain_name], var.certbot_subject_alterntive_names))
    ssh_privkey_file     = pathexpand(var.ssh_privkey_file)
    ui_version           = var.ui_version
  })
}

# local .env copy for dev purposes
resource "local_file" "dotenv" {
  content = local.dotenv_content
  #filename = "${path.module}/.env"
  file_permission = "0644"
  filename        = pathexpand(var.local_dotenv_file)
}

# remote s3 .env copy for the application
resource "aws_s3_bucket_object" "dotenv" {
  bucket        = module.s3.bucket_name
  key           = "deploy/.env"
  content       = local.dotenv_content
  storage_class = "REDUCED_REDUNDANCY"
}
