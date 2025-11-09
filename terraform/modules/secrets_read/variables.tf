
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
  description = "path for secrets not managed by tf"
  default     = "/provided"
}
