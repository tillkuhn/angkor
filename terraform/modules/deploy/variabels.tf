variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "appid" {
  description = "appid prefix for roles, users, buckets"
}

variable "bucket_name" {
  type        = string
  description = "the deployment bucket"
}

variable "topic_arn" {
  type        = string
  description = "the arn of the publish event topic"
}

//variable "bucket_path" {
//  type = string
//  description = "array of path within the bucket to grane rw permissions e.g. deploy/ "
//}
