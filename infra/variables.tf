## see also group_vars/all/vars.yml and stage specific subfolders group_vars/??/vars.yml

variable "aws_region" {
  type = "string"
  default = "eu-central-1"
}

variable "appid" {
  type = "string"
  description = "Application ID"
  default = "angkor"
}

