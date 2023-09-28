variable "app_id" {
  description = "Application ID"
}

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

