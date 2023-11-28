variable "pattern_type" {
  default = "PREFIXED"
}
variable "resource_name" {}
variable "operation" {}
variable "cluster_api_key" {}
variable "cluster_api_secret" {}
variable "cluster_id" {}
variable "cluster_endpoint" {}
variable "principal_id" {}

## more complex
#variable "topics" {
#  description = "List of Kafka Topics"
#  type = list(object({
#    name             = string
#    partitions_count = number
#    retention_hours  = number
#  }))
#  default = []
#}

