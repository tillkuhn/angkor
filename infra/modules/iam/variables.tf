variable "appid" {
  type = string
  description = "Application ID"
}

variable "bucket_name" {
  type = string
  description = "bucket name to grant access permissions"
}

variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}
