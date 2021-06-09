variable "appid" {
  type        = string
  description = "application id"
}

variable "pgp_key" {
  type        = string
  default     = ""
  description = "Either a base-64 encoded PGP public key, or a keybase username in the form keybase:some_person_that_exists"
}

variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "domain_name" {
  type        = string
  description = "primay domain name for SES domain identity resource, fully qualified"
}


variable "hosted_zone_id" {
  type        = string
  description = "hosted zone to create record for SES domain identity resource"
}

