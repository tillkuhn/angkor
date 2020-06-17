output "bucket_arn" {
  value = aws_s3_bucket.data.arn
}

output "bucket_name" {
  value = aws_s3_bucket.data.bucket
}

