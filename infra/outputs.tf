## output private ip

output "instance_info" {
  value = "${module.ec2.instance.id} ${module.ec2.user}@${module.ec2.instance.public_ip}"
}

output "ami_info" {
  value = module.ec2.ami_info
}

output "own_ip" {
  value = module.ec2.ownip
}

output "cognito_pool_endpoint" {
  value = module.cognito.pool_issuer_uri
}

