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

output "ami" {
  value = "${data.aws_ami.amazon-linux-2.name} (${data.aws_ami.amazon-linux-2.id})"
}
