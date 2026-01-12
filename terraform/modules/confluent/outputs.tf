output "schema_registry_endpoint" {
  value = data.confluent_schema_registry_cluster.main.rest_endpoint
}

output "cluster_rest_endpoint" {
  value = confluent_kafka_cluster.default.rest_endpoint
}

output "cluster_boostrap_servers" {
  value       = trimprefix(confluent_kafka_cluster.default.bootstrap_endpoint, "SASL_SSL://")
  description = "List of brokers, e.g. abc-123.eu-central-1.aws.confluent.cloud:9092 w/o SASL_SSL:// prefix"
  # SASL_SSL:// prefix needs to be removed, kafka-go and string want plain hostname and port
}

output "cluster_id" {
  value = confluent_kafka_cluster.default.id
}

output "api_key_cluster_manager" {
  value       = confluent_api_key.cluster
  description = "confluent_api_key resource for managing the Kafka cluster, single key"
}

output "api_key_producer" {
  value       = confluent_api_key.producer
  description = "confluent_api_key resource for Kafka Producer, multiple keys"
}

output "api_key_consumer" {
  value       = confluent_api_key.consumer
  description = "confluent_api_key resource for Kafka Consumer, multiple keys"
}

output "api_key_metrics" {
  value       = confluent_api_key.grafana_monitoring
  description = "API key for Grafana monitoring metrics access"
}

