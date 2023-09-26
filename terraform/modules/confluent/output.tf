output "schema_registry_endpoint" {
  value = confluent_schema_registry_cluster.main.rest_endpoint
}

output "cluster_rest_endpoint" {
  value = confluent_kafka_cluster.default.rest_endpoint
}

output "cluster_id" {
  value = confluent_kafka_cluster.default.id
}
