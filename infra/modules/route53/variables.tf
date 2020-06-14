
variable "public_ip" {
  type = string
  description = "Instance public IP for A record"
}

variable "domain_name" {
  type = string
  description = "fully qualified"
}

variable "ttl" {
  type = string
  description = "time to live in seconds"
  default = "300"
}

variable "hosted_zone_id" {
  type = string
  description = "hosted zone to create record"
}

