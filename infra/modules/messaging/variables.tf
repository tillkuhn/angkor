variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}

variable "appid" {
  description = "appid prefix for roles, users, buckets"
}

variable "message_retention_seconds" {
  type = number
  description = "The number of seconds to keep messages max 1209600 = 14d"
  default =  1209600
}

variable "max_receive_count" {
  type = number
  description = "The max_receive_count until message is put to dead letter queue"
  default =  4
}

variable "bucket_arn" {
  type = string
  description = "The bucket to grant permissions to send SNS Notifications "
}


/*
variable "s3_notification_prefix" {
  type = string
  description = "The prefix filter to trigger notification"
}



variable "bucket_name" {
  type = string
  description = "The bucket to grant permissions to send SNS Notifications "
}
*/
