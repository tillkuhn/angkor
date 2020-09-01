locals {
  tags = map("terraformModule", "param")
}

// Read: aws ssm get-parameters --names "/angkor/prod/docker_token"
resource "aws_ssm_parameter" "main" {
  name = "/${var.appid}/${var.stage}/${var.upper_key ? upper(var.key) : var.key}"
  type = var.type
  value = var.value
  description = "Managed by terraform"
  tags = merge(local.tags,var.tags)
}
