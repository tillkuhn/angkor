locals {
  tags = map("terraformModule", "ec2")
}

## s3 bucket for deploy/* artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  tags = merge(local.tags, var.tags, map("Name", "${var.aws_s3_prefix}-${var.appid}-data"))
}

resource "aws_s3_bucket" "data-dev" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-${var.dev_suffix}-data"
  tags = merge(local.tags, var.tags, map("Name", "${var.aws_s3_prefix}-${var.appid}-${var.dev_suffix}-data"))
}
