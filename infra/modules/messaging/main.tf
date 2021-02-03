## main SNS topic to which messages are pushed during 24 bukd
resource "aws_sns_topic" "events" {
  name = var.name
  display_name = "${lookup(var.tags, "appid", "default")} Events"
  tags = merge({"Name": var.name},var.tags)
  policy = <<POLICY
{
    "Version":"2012-10-17",
    "Statement":[{
        "Effect": "Allow",
        "Principal": {"AWS":"*"},
        "Action": "SNS:Publish",
        "Resource": "arn:aws:sns:*:*:${var.name}",
        "Condition":{
            "ArnLike":{"aws:SourceArn":"${var.bucket_arn}"}
        }
    }]
}
POLICY

}


## rating queue associated dead letter queue
resource "aws_sqs_queue" "events_dlq" {
  name = "${var.name}-dlq"
  message_retention_seconds = var.message_retention_seconds  ## 14d (max)
  tags = merge({"Name": "${var.name}-dlq"},var.tags)
}

## the actual rating queue
resource "aws_sqs_queue" "events" {
  name = var.name
  message_retention_seconds = var.message_retention_seconds ## 14d (max)
  receive_wait_time_seconds = var.receive_wait_time_seconds
  delay_seconds = var.delay_seconds
  redrive_policy = "{\"deadLetterTargetArn\":\"${aws_sqs_queue.events_dlq.arn}\",\"maxReceiveCount\":${var.max_receive_count}}"
  tags = merge({"Name": var.name},var.tags)
}

## permissions for SNS topic to push messages to that queue
resource "aws_sqs_queue_policy" "events_queue_policy" {
  queue_url = aws_sqs_queue.events.id
  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "First",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.events.arn}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${aws_sns_topic.events.arn}"
        }
      }
    }
  ]
}
POLICY
}

## subscribe queue to the topic
resource "aws_sns_topic_subscription" "events_subscription" {
  topic_arn = aws_sns_topic.events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.events.arn
  raw_message_delivery = "true"
}

// May become useful some day: events ...
// https://www.terraform.io/docs/providers/aws/r/s3_bucket_notification.html
/*
resource "aws_s3_bucket_notification" "docs_bucket_notification" {
  bucket = var.bucket_id
  topic {
    id = "backend-upload-event"
    topic_arn     = aws_sns_topic.events.arn
    events        = ["s3:ObjectCreated:*"]
    filter_prefix = var.s3_notification_prefix
    filter_suffix = "app.jar"
  }
  topic {
    id = "frontend-upload-event"
    topic_arn     = aws_sns_topic.events.arn
    events        = ["s3:ObjectCreated:*"]
    filter_prefix = var.s3_notification_prefix
    filter_suffix = "webapp.tgz"
  }
}
*/
