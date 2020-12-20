variable "aws_region" {
  type = string
  default = "eu-central-1"
}

variable "appid" {
  type = string
  description = "application id"
}

variable "expiry_prefix" {
  type = string
  description = "application id"
}

variable "expiry_days" {
  description = "Number of days until objects in expiry path get removed"
  default = "30"
}

variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}

variable "aws_s3_prefix" {
  type = string
  description = "Prefix for s3 buckets to make them unique e.g. domain"
}

variable "dev_suffix" {}
