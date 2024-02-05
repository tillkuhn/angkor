variable "url" {
  description = "<Grafana-instance-url> The root URL of a Grafana server. May alternatively be set via the GRAFANA_URL environment variable"
}

variable "auth" {
  description = "<Grafana-Service-Account-token> or API token, basic auth in the username:password format or anonymous (string literal). May alternatively be set via the GRAFANA_AUTH environment variable."
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

