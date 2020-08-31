variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
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
