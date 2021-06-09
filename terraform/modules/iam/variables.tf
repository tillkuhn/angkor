variable "appid" {
  type        = string
  description = "Application ID"
}

variable "bucket_name" {
  type        = string
  description = "bucket name to grant access permissions"
}

variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "topic_arn" {
  type        = string
  description = "the arn of the publish event topic"
}

variable "queue_arn" {
  type        = string
  description = "the arn of the queue to poll events"
}
