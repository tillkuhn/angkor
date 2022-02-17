output "mailer_user_name" {
  value       = aws_iam_user.mailer.name
  description = "IAM user name"
}

output "mailer_user_arn" {
  value       = aws_iam_user.mailer.arn
  description = "ARN of the IAM user"
}

output "mailer_access_key" {
  value       = aws_iam_access_key.mailer.id
  description = "IAM Access Key of the created user, used as the SMTP user name"
}

output "mailer_ses_smtp_password" {
  value       = aws_iam_access_key.mailer.ses_smtp_password_v4
  description = "The secret access key converted into an SES SMTP password"
  sensitive   = true
}

output "mailer_ses_smtp_server" {
  value = "email-smtp.${data.aws_region.current.name}.amazonaws.com"
}

output "mailer_ses_smtp_port" {
  value = "465"
}
