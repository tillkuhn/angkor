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

# spring.kafka.producer.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='Q7ZX72C6AOVZ2BHA' password='Dg4beQ5MijsEd2BibODwRlDzVC5Tlolcz1cM4FZuFcVHT0dPjIAlAG1aRPHUYoHd';
output "dev_spring_kafka_producer_sasl_jaas_config" {
  value     = "spring.kafka.producer.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='${module.confluent.dev_producer_api_key.id}' password='${module.confluent.dev_producer_api_key.secret}';"
  sensitive = true
}

output "api_token_metrics" {
  value = module.tokens.api_token_metrics
}

