## output private ip

output "instance_info" {
  value = "${module.ec2.instance.id} ${module.ec2.user}@${module.ec2.instance.public_ip}"
}

output "own_ip" {
  value = module.ec2.ownip
}

output "anu" {
  value = module.ec2.ami
}
