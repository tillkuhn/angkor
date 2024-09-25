output "schema_registry_endpoint" {
  value = data.confluent_schema_registry_cluster.main.rest_endpoint
}

output "cluster_rest_endpoint" {
  value = confluent_kafka_cluster.default.rest_endpoint
}

output "cluster_boostrap_servers" {
  # e.g. abc-123.eu-central-1.aws.confluent.cloud:9092
  # SASL_SSL:// prefix needs to be removed, kafka-go and string want plain hostname and port
  value = trimprefix(confluent_kafka_cluster.default.bootstrap_endpoint, "SASL_SSL://")
}

output "cluster_id" {
  value = confluent_kafka_cluster.default.id
}

output "app_producer_api_key" {
  value = {
    id     = confluent_api_key.app_producer_kafka_api_key.id
    secret = confluent_api_key.app_producer_kafka_api_key.secret
  }
}

output "ci_producer_api_key" {
  value = {
    id     = confluent_api_key.ci_producer_kafka_api_key.id
    secret = confluent_api_key.ci_producer_kafka_api_key.secret
  }
}

output "app_consumer_api_key" {
  value = {
    id     = confluent_api_key.app_consumer_kafka_api_key.id
    secret = confluent_api_key.app_consumer_kafka_api_key.secret
  }
}

output "topic_acl_group_prefix" {
  value = confluent_kafka_acl.app_consumer_read_group.resource_name
}
