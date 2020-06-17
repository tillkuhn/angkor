locals {
  tags = map("terraformModule", "iam")
}

## iam role for ec2 see also https://medium.com/@devopslearning/aws-iam-ec2-instance-role-using-terraform-fa2b21488536
resource "aws_iam_role" "instance_role" {
  name = "${var.appid}-instance-role"
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
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-data"))
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
      "Resource": [ "arn:aws:s3:::${var.bucket_name}" ]
    },
    {
      "Sid": "AllowInstanceToSyncBucket",
      "Effect": "Allow",
      "Action": [
        "s3:DeleteObject",
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:ListBucket",
        "s3:PutObject"
      ],
      "Resource": [ "arn:aws:s3:::${var.bucket_name}/*" ]
    }
  ]
}
EOF
}
