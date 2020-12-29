output "instance" {
  value = aws_instance.instance
  description = "The generated instance"
}

output "user" {
  value = "ec2-user"
  # should be dynamic
  description = "The generated instance"
}

output "ownip" {
  value = chomp(data.http.ownip.body)
  # should be dynamic
  description = "Your own ip for ssh ingress"
}

output "ami_info" {
  value = "AMI: ${data.aws_ami.amazon-linux-2.name} RecentID: ${data.aws_ami.amazon-linux-2.id} dd ${data.aws_ami.amazon-linux-2.creation_date} CurrentID: ${aws_instance.instance.ami}"
}
