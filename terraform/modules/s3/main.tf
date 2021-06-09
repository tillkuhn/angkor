locals {
  tags = map("terraformModule", "ec2")
}

## s3 bucket for deploy/* artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  # let objects automatically expire after x days
  lifecycle_rule {
    id = "expire_db_dumps"
    enabled = true
    prefix = var.expiry_prefix # optional, uncomment to apply to entire bucket
    expiration {
      days = var.expiry_days
    }
  }
  tags = merge(local.tags, var.tags, map("Name", "${var.aws_s3_prefix}-${var.appid}-data"))
}

resource "aws_s3_bucket" "data_dev" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data-${var.dev_suffix}"
  tags = merge(local.tags, var.tags, map("Name", "${var.aws_s3_prefix}-${var.appid}-data-${var.dev_suffix}"))
}
