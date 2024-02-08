variable "app_id" {
  description = "Application ID"
}

variable "keeper" {
  description = "Values that, when changed, will trigger recreation of resource."
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

