variable "tags" {
  type = map
  description = "Tags to be attached to the resource, Name tag will be added by the module"
  default = {}
}

variable "id" {
  type = string
  description = "Version to which the random name is tied as Keeper"
}

variable "stage" {
  default = "prod"
}

variable "appid" {
}

