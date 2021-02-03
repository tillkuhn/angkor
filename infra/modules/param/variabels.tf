variable "tags" {
  type = map
  description = "Tags to be attached to the resource, Name tag will be added by the module"
  default = {}
}

variable "appid" {
  description = "appid prefix for roles, users, buckets"
}

variable "key" {}
variable "value" {}
variable "stage" {
  default = "prod"
}

variable "type" {
  default = "SecureString"
  description = "Type, defaults to SecureString, but String is also allowed"
}

variable "upper_key" {
  type = bool
  description = "whethere to change key automatically to uppercase"
  default = false
}
