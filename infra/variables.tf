# common
variable "aws_region" {
  type    = string
  default = "eu-central-1"
}

variable "appid" {
  type        = string
  description = "Application ID"
}

# ec2
variable "aws_vpc_name" {
  type        = string
  description = "Name tag of your vpc"
}

variable "aws_subnet_name" {
  type        = string
  description = "Name tag of your subnet"
}

variable "aws_instance_type" {
  type        = string
  description = "type of the EC2 instance"
  default     = "t3a.nano"
}

## Amazon Linux 2 AMI (HVM), SSD Volume Type (64-bit x86)
variable "aws_instance_ami" {
  type    = string
  default = "ami-0f3a43fbf2d3899f7"
  ## aws linux
}

variable "ssh_pubkey_file" {
  description = "location of your public key which will be used for keypair, may contain ~"
}
variable "ssh_privkey_file" {
  description = "location of your privkey whose value will stay local (only for scripting), may contain ~"
}

# route53
variable "hosted_zone_id" {
  type        = string
  description = "hosted zone to create record"
}

variable "certbot_domain_name" {
  type        = string
  description = "fully qualified domain name"
}

variable "certbot_subject_alterntive_names" {
  type    = list(string)
  default = []
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

## app secrets
variable "db_url" {}
variable "db_username" {}
variable "db_password" {}
variable "api_version" {}
variable "ui_version" {}

## dockerhub
variable "docker_token" {}
variable "docker_user" {}

## mapbox
variable "mapbox_access_token" {}
