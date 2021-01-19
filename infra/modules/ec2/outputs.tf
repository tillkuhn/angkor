output "instance_id" {
  value = aws_instance.instance.id
  description = "The id of the generated instance"
}

output "instance_name" {
  value = aws_instance.instance.tags["Name"]
  description = "The Name of the generated instance"
}

output "public_ip" {
  value = aws_eip_association.eip_assoc.public_ip
  description = "The public IP of the elastic ip address"
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

output "api_token" {
  value = random_uuid.api_token.id
  # should be dynamic
  description = "The generated API Token which is kept until AMI Changes"
}

output "ami_info" {
  value = "AMI: ${data.aws_ami.amazon-linux-2.name} RecentID: ${data.aws_ami.amazon-linux-2.id} dd ${data.aws_ami.amazon-linux-2.creation_date} CurrentID: ${aws_instance.instance.ami}"
}
