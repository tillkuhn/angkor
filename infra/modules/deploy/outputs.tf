output "user_name" {
  value       = aws_iam_user.deploy.name
  description = "The name of the deployment user"
}
