variable "tags" {
  type        = map(any)
  description = "Tags to be attached to the resource, Name tag will be added by the module"
  default     = {}
}

variable "id" {
  type        = string
  description = "Version aka releaseVersion to which the random name is tied to as Keeper"
}

variable "stage" {
  default = "prod"
}

variable "appid" {
}

