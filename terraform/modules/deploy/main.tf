#https://www.terraform.io/docs/providers/aws/r/iam_user.html
locals {
  tags = tomap({"terraformModule"= "deploy"})
}

data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

resource "aws_iam_user" "deploy" {
  name = "${var.appid}-deploy"
  #path = "/system/"
  tags = merge(local.tags,var.tags)
}

//resource "aws_iam_access_key" "deploy" {
//    user = aws_iam_user.deploy.name
//}

resource "aws_iam_user_policy" "deploy" {
  name = "${var.appid}-deploy-policy"
  user = aws_iam_user.deploy.name

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
      "Sid": "AllowDeployUserToUploadFiles",
      "Effect": "Allow",
      "Action": [
        "s3:DeleteObject",
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:ListBucket",
        "s3:PutObject"
      ],
      "Resource": [
         "arn:aws:s3:::${var.bucket_name}/deploy/docs/*",
         "arn:aws:s3:::${var.bucket_name}/deploy/tools/*"
        ]
    },
    {
      "Sid": "AllowDeployUserToPublishEvents",
      "Effect": "Allow",
      "Action": "sns:Publish",
      "Resource": "${var.topic_arn}"
    },
    {
      "Effect": "Allow",
      "Sid": "AllowDeployUserToReadSelectedSsmParams",
      "Action": [
          "ssm:GetParameter*"
      ],
      "Resource": [
        "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.appid}/*/RELEASE*",
        "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.appid}/*/SONAR*"
      ]
    }
  ]
}
EOF
}
