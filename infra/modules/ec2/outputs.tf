output "ec2instance" {
  value       = aws_instance.instance
  description = "The generated instance"
}
