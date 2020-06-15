locals {
  tags = map("terraformModule", "ec2")
}

## s3 bucket for deploy/* artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  region = var.aws_region
  tags = merge(local.tags,var.tags,map("Name", "${var.appid}-data"))
}
