# Inspired by
# https://github.com/confluentinc/terraform-provider-confluent/blob/master/examples/configurations/basic-kafka-acls/main.tf

resource "confluent_kafka_acl" "app_consumer_read_sys_topic" {
  kafka_cluster {
    id = var.cluster_id
  }
  rest_endpoint = var.cluster_endpoint
  resource_type = "TOPIC"
  resource_name = var.resource_name

  # pattern_type to be one of [UNKNOWN ANY MATCH LITERAL PREFIXED]
  # see https://docs.confluent.io/platform/current/kafka/authorization.html#use-prefixed-acls
  # If you identify the resource as LITERAL, Kafka will attempt to match the full resource name
  # (in some cases, you might want to use an asterisk (*) to specify all resources)
  # If you identify the resource as PREFIXED, Kafka attempts to match the prefix of the resource name with the resource specified in ACL.
  pattern_type = var.pattern_type

  # operations: UNKNOWN, ANY, ALL, READ, WRITE, CREATE, DELETE, ALTER, DESCRIBE,
  # CLUSTER_ACTION, DESCRIBE_CONFIGS, ALTER_CONFIGS, and IDEMPOTENT_WRITE.
  operation  = var.operation
  principal  = "User:${var.principal_id}"
  host       = "*"
  permission = "ALLOW"
  credentials {
    key    = var.cluster_api_key
    secret = var.cluster_api_secret
  }
}
