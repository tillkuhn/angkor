variable "app_id" {
  description = "Application ID"
}

variable "project_id" {
  description = "Project ID in DB SaaS Service (e.g. neon)"
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

