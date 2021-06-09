variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "name" {
  description = "base name of the queue, including (if desired) app id prefix"
}

variable "message_retention_seconds" {
  type        = number
  description = "The number of seconds to keep messages max 1209600 = 14d"
  default     = 1209600
}

variable "max_receive_count" {
  type        = number
  description = "The max_receive_count until message is put to dead letter queue"
  default     = 4
}

variable "bucket_arn" {
  type        = string
  description = "The bucket to grant permissions to send SNS Notifications "
}

#
variable "receive_wait_time_seconds" {
  type        = string
  default     = "20"
  description = "The receive message wait time is the maximum amount of time that polling will wait for messages to become available to receive"
}

variable "delay_seconds" {
  type        = string
  default     = "0"
  description = "If your consumers need additional time to process messages, you can delay each new message coming to the queue. "
}
