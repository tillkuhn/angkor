###################################################
# manage local and remote remote files
###################################################

# docker-compose.yml which handles everything managed by docker on ec2
resource "aws_s3_bucket_object" "dockercompose" {
  bucket        = module.s3.bucket_name
  key           = "deploy/docker-compose.yml"
  content       = file("${path.module}/files/docker-compose.yml")
  storage_class = "REDUCED_REDUNDANCY"
}

# appctl.sh is our main control script on ec2 for various taks
resource "aws_s3_bucket_object" "deployscript" {
  bucket        = module.s3.bucket_name
  key           = "deploy/appctl.sh"
  content       = file("${path.module}/files/appctl.sh")
  storage_class = "REDUCED_REDUNDANCY"
}

# store useful ENV vars in dotenv_content, then create local and remote version
locals {
  dotenv_content = templatefile("${path.module}/templates/.env", {
    account_id           = module.vpcinfo.account_id
    aws_region           = module.vpcinfo.aws_region
    api_version          = var.api_version
    api_token            = module.ec2.api_token
    appid                = var.appid
    bucket_name          = module.s3.bucket_name
    certbot_domain_name  = var.certbot_domain_name
    certbot_domain_str   = format("-d %s", join(" -d ", concat([var.certbot_domain_name], var.certbot_subject_alterntive_names)))
    certbot_mail         = var.certbot_mail
    db_password          = var.db_password
    db_url               = var.db_url
    db_username          = var.db_username
    db_api_key           = var.db_api_key
    sonar_login          = var.sonar_login
    sonar_token          = var.sonar_token
    sonar_password       = var.sonar_password
    docker_token         = var.docker_token
    docker_user          = var.docker_user
    imprint_url          = var.imprint_url
    instance_id          = module.ec2.instance_id
    mapbox_access_token  = var.mapbox_access_token
    oauth2_client_id     = module.cognito.app_client_id
    oauth2_client_name   = module.cognito.app_client_name
    oauth2_client_secret = module.cognito.app_client_secret
    oauth2_issuer_uri    = module.cognito.pool_issuer_uri
    public_ip            = module.ec2.public_ip
    server_names         = join(" ", concat([var.certbot_domain_name], var.certbot_subject_alterntive_names))
    ssh_privkey_file     = pathexpand(var.ssh_privkey_file)
    ui_version           = var.ui_version
    smtp_user            = module.ses.mailer_access_key
    smtp_password        = module.ses.mailer_ses_smtp_password
    smtp_server          = module.ses.mailer_ses_stmp_server
    smtp_port            = module.ses.mailer_ses_stmp_port
  })
}

# local .env copy in ~/.anngkor/.env for for dev purposes and parent Makefile
resource "local_file" "dotenv" {
  content         = local.dotenv_content
  file_permission = "0644"
  filename        = pathexpand(var.local_dotenv_file)
}

# remote s3 .env in /home/ec2user for the docker-compose and friends
resource "aws_s3_bucket_object" "dotenv" {
  bucket        = module.s3.bucket_name
  key           = "deploy/.env"
  content       = local.dotenv_content
  storage_class = "REDUCED_REDUNDANCY"
}
