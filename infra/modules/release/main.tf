
# Generate a new pet name each time we switch to a new AMI id
resource "random_pet" "release" {
  keepers = {
    version = var.id
  }
}

// Read: aws ssm get-parameters --names "/angkor/prod/docker_token"
resource "aws_ssm_parameter" "release" {
  name = "/${var.appid}/${var.stage}/RELEASE_NAME"
  type = "String"
  value = random_pet.release.id
  description = "Managed by terraform"
  tags = var.tags
}


