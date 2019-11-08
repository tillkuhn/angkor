
provider "aws" {
  region = "eu-central-1"
  version = "~> 2.34"
}

## see terraform-backend.tf.tmpl for s3 backend terraform state

data "aws_vpc" "vpc" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_vpc_name]
  }
}

data "aws_subnet" "app_onea" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_subnet_name]
  }
  vpc_id = data.aws_vpc.vpc.id
}

data "aws_security_group" "ssh" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_ssh_security_group_name]
  }
}
## SSH key for instance (BYOK)
resource "aws_key_pair" "ssh_key" {
  key_name    = var.appid
  public_key  = file(var.ssh_pubkey_file)
}

## bucket for artifacts
resource "aws_s3_bucket" "data" {
  bucket = "${var.aws_s3_prefix}-${var.appid}-data"
  region = var.aws_region
  tags = map("Name", "${var.appid}-data", "appid", var.appid, "managedBy", "terraform")
}

## iam role for ec2 see also https://medium.com/@devopslearning/aws-iam-ec2-instance-role-using-terraform-fa2b21488536
resource "aws_iam_role" "instance_role" {
  name = "${var.appid}-instance-role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
  tags = map("Name", "${var.appid}-instance-role", "appid", var.appid, "managedBy", "terraform")
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
    }
  ]
}
EOF
}

resource "aws_iam_instance_profile" "instance_profile" {
  name = "${var.appid}-instance-profile"
  role = aws_iam_role.instance_role.name
}

## security group for ec2
resource "aws_security_group" "instance_sg" {
  name        = "${var.appid}-instance-sg"
  description = "Security group for ${var.appid} instances"
  vpc_id      = "${data.aws_vpc.vpc.id}"
//  ingress {
//    # ingress rule for SSH communication
//    from_port = 22
//    to_port = 22
//    protocol = "tcp"
//    security_groups = ["${data.aws_security_group.bastion.id}"]
//  }
//  ingress {
//    # ingress rule for HTTP communication
//    from_port = 80
//    to_port = 80
//    protocol = "tcp"
//    security_groups = ["${data.aws_security_group.alb_sg.id}"]
//  }
  ingress {
    # allow echo / ping requests
    from_port = 8
    to_port = -1
    protocol = "icmp"
    cidr_blocks = ["10.0.0.0/8"]
  }
  ingress {
    # ingress rule for HTTP communication
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    # ingress rule for HTTPS communication
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    # allow all egress rule
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = map("Name", "${var.appid}-instance-sg","appid", var.appid,  "managedBy", "terraform")
}

//## Actual EC2 instance
resource "aws_instance" "instance" {
  ami                     = var.aws_instance_ami
  instance_type           = var.aws_instance_type
  iam_instance_profile = aws_iam_instance_profile.instance_profile.name
  vpc_security_group_ids  = [ aws_security_group.instance_sg.id, data.aws_security_group.ssh.id ]
  subnet_id               = data.aws_subnet.app_onea.id
  key_name                = aws_key_pair.ssh_key.key_name
  ## User data is limited to 16 KB, in raw form, before it is base64-encoded.
  ## The size of a string of length n after base64-encoding is ceil(n/3)*4.
  ## curl http://169.254.169.254/latest/user-data
  ## cat cat /var/log/cloud-init-output.log
  user_data =  templatefile("${path.module}/user_data.sh", { certbot_mail = var.certbot_mail, domain_name=var.domain_name, bucket_name=aws_s3_bucket.data.bucket})
  tags = map("Name", "${var.appid}-instance", "appid", var.appid, "managedBy", "terraform")
  lifecycle {
    ignore_changes = [ "ami" ]
  }
}


## Route 53 ALB Alias for ssh access to compute instance
resource "aws_route53_record" "instance_dns" {
  zone_id = var.hosted_zone_id
  name    = var.domain_name  ## fully qualified
  type    = "A"
  records = [aws_instance.instance.public_ip]
  ttl = "300"
}

## output private ip
output "instance_ip" {
  value = "private ${aws_instance.instance.private_ip} public ${aws_instance.instance.public_ip} id ${aws_instance.instance.id}"
}

