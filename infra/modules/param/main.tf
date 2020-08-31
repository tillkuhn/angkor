locals {
  tags = map("terraformModule", "param")
}

// Read: aws ssm get-parameters --names "/angkor/prod/docker_token"
resource "aws_ssm_parameter" "main" {
  name = "/${var.appid}/${var.stage}/${var.key}"
  type = "SecureString"
  value = var.value
  description = "Managed by terraform"
  tags = merge(local.tags,var.tags)
}
