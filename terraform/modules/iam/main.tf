locals {
  tags = tomap({ "terraformModule" = "iam" })
}

data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

## iam role for EC2 Instance, scroll down for policy
## see also https://medium.com/@devopslearning/aws-iam-ec2-instance-role-using-terraform-fa2b21488536
resource "aws_iam_role" "instance_role" {
  name               = "${var.appid}-instance-role"
  assume_role_policy = <<-EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "AllowEC2InstancesToAssumeThisRole",
        "Action": "sts:AssumeRole",
        "Principal": {
          "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow"
      }
    ]
  }
  EOF
  tags               = merge(local.tags, var.tags, tomap({ "Name" = "${var.appid}-data" }))
}

resource "aws_iam_instance_profile" "instance_profile" {
  name = "${var.appid}-instance-profile"
  role = aws_iam_role.instance_role.name
}

resource "aws_iam_role_policy" "instance_policy" {
  name   = "${var.appid}-instance-policy"
  role   = aws_iam_role.instance_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowInstanceToListBuckets",
      "Action": ["s3:ListBucket"],
      "Effect": "Allow",
      "Resource": [ "arn:aws:s3:::${var.bucket_name}" ]
    },
    {
      "Sid": "AllowInstanceToSyncBucket",
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket",
        "s3:GetBucketLocation",
        "s3:DeleteObject",
        "s3:GetObject*",
        "s3:PutObject*"
      ],
      "Resource": [ "arn:aws:s3:::${var.bucket_name}/*" ]
    },
    {
      "Effect": "Allow",
      "Sid": "AllowInstanceToDescribeSsmParams",
      "Action": [
          "ssm:DescribeParameters"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Sid": "AllowInstanceToReadSsmParams",
      "Action": [
          "ssm:GetParameter*"
      ],
      "Resource": "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.appid}*"
    },
    {
      "Effect": "Allow",
      "Sid": "AllowInstanceToDescribeTags",
      "Action": "ec2:DescribeTags",
      "Resource": "*"
    },
    {
      "Sid": "AllowDeployUserToPublishEvents",
      "Effect": "Allow",
      "Action": "sns:Publish",
      "Resource": "${var.topic_arn}"
    },
    {
      "Sid": "AllowDeployUserToPollEvents",
      "Effect": "Allow",
      "Action": "sqs:*",
      "Resource": "${var.queue_arn}"
    },
    {
        "Sid": "AllowListAndDescribeDynamoDBTables",
        "Effect": "Allow",
        "Action": [
            "dynamodb:List*",
            "dynamodb:DescribeReservedCapacity*",
            "dynamodb:DescribeLimits",
            "dynamodb:DescribeTimeToLive"
        ],
        "Resource": "*"
    },
    {
        "Sid": "AllowFullAccessToDynamoDBTablesWithAppIdPrefix",
        "Effect": "Allow",
        "Action": [
            "dynamodb:BatchGet*",
            "dynamodb:DescribeStream",
            "dynamodb:DescribeTable",
            "dynamodb:Get*",
            "dynamodb:Query",
            "dynamodb:Scan",
            "dynamodb:BatchWrite*",
            "dynamodb:CreateTable",
            "dynamodb:Delete*",
            "dynamodb:Update*",
            "dynamodb:PutItem"
        ],
        "Resource": "arn:aws:dynamodb:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:table/${var.appid}*"
    }
  ]
}
EOF
}
