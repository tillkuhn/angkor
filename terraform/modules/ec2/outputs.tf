output "instance_id" {
  value       = aws_instance.instance.id
  description = "The id of the generated instance"
}

output "instance_name" {
  value       = aws_instance.instance.tags["Name"]
  description = "The Name of the generated instance"
}

output "public_ip" {
  value       = aws_eip_association.eip_assoc.public_ip
  description = "The public IP of the elastic ip address"
}

output "user" {
  value = "ec2-user"
  # should be dynamic
  description = "The generated instance"
}

output "ownip" {
  value = chomp(module.vpcinfo.ownip)
  # should be dynamic
  description = "Your own ip for ssh ingress"
}

output "api_token" {
  value       = random_uuid.api_token.id
  description = "Random API token which we can share will other services for internal API communication, uses AMI ID as keeper to keep it stable for some time but ensure rotation"
}

output "ami_info" {
  value = format("%s\n%s\n%s (changed: %s)",
    "Current AMI Name.....: ${data.aws_ami.amazon-linux-2.name}",
    "Current AMI ID.......: ${aws_instance.instance.ami}",
    "Most recent available: ${data.aws_ami.amazon-linux-2.id} dd ${data.aws_ami.amazon-linux-2.creation_date}",
  aws_instance.instance.ami != data.aws_ami.amazon-linux-2.id)
}

# AMI ID, useful as stable "keeper" for random resources, e.g. random_uuid
output "ami_id" {
  value = data.aws_ami.amazon-linux-2.id
}
