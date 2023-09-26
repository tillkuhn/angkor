resource "confluent_kafka_topic" "topic" {
  kafka_cluster {
    id = var.cluster_id
  }
  topic_name       = var.name # + var.suffix
  partitions_count = var.partitions_count
  rest_endpoint    = var.cluster_endpoint
  config = {
    "cleanup.policy"    = "delete"                      # [compact, delete]
    "max.message.bytes" = var.max_bytes                 #  250000 = 250kb, default "604800000"
    "retention.ms"      = 3600000 * var.retention_hours # 86400000ms  = 1 day = 3600000 hours
  }
  credentials {
    # use a cluster api key here (with appropriate topic permissions, e.g. for the prefix ttb-*)
    key    = var.cluster_api_key
    secret = var.cluster_api_secret
  }
}
