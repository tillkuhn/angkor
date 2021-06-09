output "arn" {
  value       = aws_dynamodb_table.table.arn
  description = "The name of the Auto Scaling Group"
}
