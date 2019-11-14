
## iam role for ec2 see also https://medium.com/@devopslearning/aws-iam-ec2-instance-role-using-terraform-fa2b21488536
resource "aws_iam_role" "instance_role" {
  name = "${var.appid}-instance-role"

  assume_role_policy = <<EOF
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
  tags = map("Name", "${var.appid}-instance-role", "appid", var.appid, "managedBy", "terraform")
}


resource "aws_iam_instance_profile" "instance_profile" {
  name = "${var.appid}-instance-profile"
  role = aws_iam_role.instance_role.name
}

resource "aws_iam_role_policy" "instance_policy" {
  name = "${var.appid}-instance-policy"
  role = aws_iam_role.instance_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowInstanceToListBuckets",
      "Action": ["s3:ListBucket"],
      "Effect": "Allow",
      "Resource": [ "arn:aws:s3:::${aws_s3_bucket.data.bucket}" ]
    },
    {
      "Sid": "AllowInstanceToSyncBucket",
      "Effect": "Allow",
      "Action": [
        "s3:DeleteObject",
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [ "arn:aws:s3:::${aws_s3_bucket.data.bucket}/*" ]
    },
    {
      "Sid": "AllowInstanceToWriteToBucketAppData",
      "Effect": "Allow",
      "Action": [
        "s3:DeleteObject",
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:ListBucket",
        "s3:PutObject"
      ],
      "Resource": [
      "arn:aws:s3:::${aws_s3_bucket.data.bucket}/appdata/*",
      "arn:aws:s3:::${aws_s3_bucket.data.bucket}/letsencrypt/*"]
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
        "Sid": "AllowFullAccessToTablesStartingWithAppId",
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
        "Resource": "arn:aws:dynamodb:*:*:table/${var.appid}-*"
    }
  ]
}
EOF
}
