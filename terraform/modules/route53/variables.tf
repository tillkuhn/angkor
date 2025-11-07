variable "public_ip" {
  type        = string
  description = "Instance public IP for A record"
  validation {
    condition     = can(regex("^([0-9]{1,3}\\.){3}[0-9]{1,3}$", var.public_ip))
    error_message = "The public_ip variable must be a valid IPv4 address."
  }
}

variable "domain_name" {
  type        = string
  description = "primary domain name, fully qualified"
  validation {
    condition     = length(var.domain_name) > 0
    error_message = "The domain_name variable must not be empty."
  }
}

variable "subject_alternative_names" {
  type        = list(string)
  description = "list of fully qualified SANs"
}

variable "ttl" {
  type        = string
  description = "Time to live in seconds"
  default     = "300"
}

variable "hosted_zone_id" {
  type        = string
  description = "Hosted zone to create record"
}

