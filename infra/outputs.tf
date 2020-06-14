## output private ip

output "instance_id" {
  value = "Instance Id: ${module.ec2.instance.id}"
}

output "ssh_string" {
  value = "ssh -i ${local.ssh_privkey_file} ${module.ec2.user}@${module.ec2.instance.public_ip}"
}

output "ownip" {
  value = module.ec2.ownip
}

