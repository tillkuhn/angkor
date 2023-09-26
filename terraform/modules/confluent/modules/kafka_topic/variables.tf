variable "name" {}
variable "cluster_api_key" {}
variable "cluster_api_secret" {}
variable "cluster_id" {}
variable "cluster_endpoint" {}


#variable "suffix" {
#  description = "optional suffix for kafka topics, e.g. .dev"
#  default     = ""
#}
variable "retention_hours" {
  description = "retention days to keep messages in a kafka topic"
  default     = 24
}
variable "partitions_count" {
  description = "number of kafka partitions, default 1"
  default     = 1
}
variable "max_bytes" {
  description = "max bytes per message in kafka topic"
  default     = 500000
}
