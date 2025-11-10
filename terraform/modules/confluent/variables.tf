variable "app_id" {
  description = "Application ID"
}

variable "env_id" {
  description = "Environment ID"
}

variable "cloud_api_key" {
  description = "Cloud API Key with organizational privileges to boostrap the initial environment"
}
variable "cloud_api_secret" {
  description = "Cloud API Secret with org privileges to manage resources"
}

variable "topics" {
  description = "List of managed Kafka Topics"
  type = list(object({
    name             = string
    partitions_count = number
    retention_hours  = number
  }))
  default = []
}

variable "service_accounts_consumer" {
  description = "Map of producer service accounts with their respective Kafka Topic ACLs"
  type = map(object({
    name         = optional(string)
    acl_prefixes = list(string)
  }))
  default = {}
}

variable "service_accounts_producer" {
  description = "Map of consumer service accounts with their respective Kafka Topic ACLs"
  type = map(object({
    name         = optional(string)
    acl_prefixes = list(string)
  }))
  default = {}
}


variable "topic_acl_app_prefix" {
  default = "app."
}

variable "topic_acl_dev_prefix" {
  default = "dev."
}

variable "topic_acl_system_prefix" {
  default = "system."
}

variable "topic_acl_ci_prefix" {
  default = "ci."
}

variable "topic_acl_public_prefix" {
  default = "public."
}

variable "topic_acl_group_prefix" {
  default     = "app."
  description = "Prefix for consumer group ACL"
}
