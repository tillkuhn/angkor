variable "app_id" {
  description = "Application ID"
}

variable "env_id" {
  description = "Environment ID"
}

variable "cloud_api_key" {
  description = "Cloud API Key with organizational privileges (need to create the initial environment)"
}
variable "cloud_api_secret" {
  description = "Corresponding Cloud API Secret"
}

variable "topics" {
  description = "List of Kafka Topics"
  type = list(object({
    name             = string
    partitions_count = number
    retention_hours  = number
  }))
  default = []
}

variable "hcp_vault_secrets_app_name" {
  description = "App Name to store Vault Secrets, will be created and managed by this module"
  default     = "confluent"
}
