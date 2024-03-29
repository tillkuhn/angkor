## useful outputs after each terraform run

output "instance_info" {
  value = "${module.ec2.instance_id} (${module.ec2.instance_name}) ${module.ec2.user}@${module.ec2.public_ip}"
}

output "ami_info" {
  value = module.ec2.ami_info
}

output "own_ip" {
  value = module.ec2.ownip
}

output "release_name" {
  value = module.release.name
}

output "release_version" {
  value = module.release.version
}

output "confluent_cluster_rest_endpoint" {
  value = module.confluent.cluster_rest_endpoint
}
output "confluent_cluster_id" {
  value = module.confluent.cluster_id
}

output "api_token_metrics" {
  value = module.tokens.api_token_metrics
}
