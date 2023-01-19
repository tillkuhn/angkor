locals {
  tags = tomap({ "terraformModule" = "s3" })
}

## s3 bucket for deploy/* artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  tags   = merge(local.tags, var.tags, tomap({ "Name" = "${var.aws_s3_prefix}-${var.appid}-data" }))
}

# let objects automatically expire after x days, v4 provider style
resource "aws_s3_bucket_lifecycle_configuration" "data" {
  bucket = aws_s3_bucket.data.id
  rule {
    id     = "expire_db_dumps"
    status = "Enabled"
    filter {
      prefix = var.expiry_prefix # optional, uncomment to apply to entire bucket
    }
    expiration {
      days = var.expiry_days
    }
  }
}

resource "aws_s3_bucket" "data_dev" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data-${var.dev_suffix}"
  tags   = merge(local.tags, var.tags, tomap({ "Name" = "${var.aws_s3_prefix}-${var.appid}-data-${var.dev_suffix}" }))
}
