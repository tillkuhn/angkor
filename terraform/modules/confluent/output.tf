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

output "api_key_producer" {
  value = confluent_api_key.producer
}

output "api_key_consumer" {
  value = confluent_api_key.consumer
}

