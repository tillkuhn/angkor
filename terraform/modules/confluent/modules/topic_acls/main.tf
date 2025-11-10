# Inspired by
# https://github.com/confluentinc/terraform-provider-confluent/blob/master/examples/configurations/basic-kafka-acls/main.tf

# Note that in order to consume from a topic, the principal of the consumer ('app-consumer' service account)
# needs to be authorized to perform 'READ' operation on both TOPIC and (Consumer) GROUP resources:
# confluent_kafka_acl.app-consumer-read-on-topic, confluent_kafka_acl.app-consumer-read-on-group.
# https://docs.confluent.io/platform/current/kafka/authorization.html#using-acls

resource "confluent_kafka_acl" "acl" {
  for_each = toset(var.resource_names)
  kafka_cluster {
    id = var.cluster_id
  }
  rest_endpoint = var.cluster_endpoint
  resource_type = var.resource_type
  resource_name = each.key
  pattern_type  = var.pattern_type # see vars for allowed values

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
