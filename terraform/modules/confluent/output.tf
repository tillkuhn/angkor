output "schema_registry_endpoint" {
  value = confluent_schema_registry_cluster.main.rest_endpoint
}

output "cluster_rest_endpoint" {
  value = confluent_kafka_cluster.default.rest_endpoint
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
