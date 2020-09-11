output "topic_arn" {
  value       = aws_sns_topic.events.arn
  description = "The arn of the SNS events topic"
}

output "queue_arn" {
  value       = aws_sqs_queue.events.arn
  description = "The arn of the SQS events Queue"
}
