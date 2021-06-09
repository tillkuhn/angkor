output "instance_profile_name" {
  value       = aws_iam_instance_profile.instance_profile.name
  description = "The instance profile which can be used for EC2"
}

