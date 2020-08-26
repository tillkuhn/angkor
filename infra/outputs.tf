## output private ip

output "instance_info" {
  value = "${module.ec2.instance.id} ${module.ec2.user}@${module.ec2.instance.public_ip}"
}

output "own_ip" {
  value = module.ec2.ownip
}

output "cognito_pool_id" {
  value = module.cognito.pool_id
}

output "cognito_pool_endpoint" {
  value = module.cognito.pool_endpoint
}

output "cognito_pool_client_id" {
  value = module.cognito.pool_client_id
}

