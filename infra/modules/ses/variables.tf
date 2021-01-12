variable "appid" {
  type = string
  description = "application id"
}

variable "pgp_key" {
  type        = string
  default = ""
  description = "Either a base-64 encoded PGP public key, or a keybase username in the form keybase:some_person_that_exists"
}

variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}

