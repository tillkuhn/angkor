# variable "vault_secrets_app_name" {
#   description = "name of the HCP Secrets Vault (1 App = 1 Vault)"
# }

# variable "vault_secrets_app_description" {
#   description = "Optional Description"
#   default     = ""
# }


variable "upper_key" {
  type        = bool
  description = "whether to change key automatically to uppercase"
  default     = true
}

variable "secrets" {
  description = "List of Secrets"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "app_id" {
  description = "phase app id (uuid)"
}


variable "env" {
  default = "development"
  validation {
    condition     = contains(["development", "staging", "production"], var.env)
    error_message = "The value for 'env' must be one of: development, staging, production."
  }
}

variable "path" {
  default = "/tfwrite"
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

