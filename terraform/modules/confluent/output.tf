output "schema_registry_endpoint" {
  value = confluent_schema_registry_cluster.main.rest_endpoint
}
