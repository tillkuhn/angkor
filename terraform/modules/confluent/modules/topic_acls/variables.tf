# pattern_type to be one of [UNKNOWN ANY MATCH LITERAL PREFIXED]
# see https://docs.confluent.io/platform/current/kafka/authorization.html#use-prefixed-acls
# If you identify the resource as LITERAL, Kafka will attempt to match the full resource name
# (in some cases, you might want to use an asterisk (*) to specify all resources)
# If you identify the resource as PREFIXED, Kafka attempts to match the prefix of the resource name with the resource specified in ACL.
variable "pattern_type" {
  default = "PREFIXED"
  validation {
    condition     = contains(["PREFIXED", "LITERAL", "UNKNOWN", "ANY", "MATCH"], var.pattern_type)
    error_message = "Only PREFIXED and LITERAL are allowed."
  }
}
variable "resource_type" {
  default = "TOPIC"
  validation {
    condition     = contains(["TOPIC", "GROUP"], var.resource_type)
    error_message = "Only TOPIC and GROUP are allowed."
  }
}
variable "resource_names" {
  description = "list of resource names or prefixes (topics) for the ACLs"
  type        = list(string)
  validation {
    condition     = length(var.resource_names) > 0
    error_message = "The resource_name list must contain at least one element."
  }
}

variable "operation" {
  description = "ACL operation to be applied, e.g. READ, WRITE, CREATE, DELETE, etc."
  validation {
    condition     = contains(["UNKNOWN", "ANY", "ALL", "READ", "WRITE", "ALTER", "DESCRIBE", "ALTER_CONFIGS", "DESCRIBE_CONFIGS", "CREATE", "DELETE"], var.operation)
    error_message = "The operation must be one of: READ, WRITE, ALTER."
  }

}

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

