# module specific local vars
locals {
  tags = map("terraformModule", "release")
}

# Generate a new pet name each time we switch to a new AMI id
resource "random_pet" "release" {
  keepers = {
    version = var.id
  }
}

// Read: aws ssm get-parameters --names "/angkor/prod/docker_token"
resource "aws_ssm_parameter" "release" {
  for_each = {
    RELEASE_NAME = random_pet.release.id
    RELEASE_VERSION = var.id
  }
  name = "/${var.appid}/${var.stage}/${each.key}"
  value = each.value
  type = "String"
  description = "Managed by terraform"
  tags = merge(local.tags, var.tags)
}


