# module specific vars
locals {
  tags = map("terraformModule", "ec2")
}

## locate existing vpc by name
data "aws_vpc" "vpc" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_vpc_name]
  }
}

## target subnet for ec2 instance
data "aws_subnet" "app_onea" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_subnet_name]
  }
  vpc_id = data.aws_vpc.vpc.id
}

# https://stackoverflow.com/questions/46763287/i-want-to-identify-the-public-ip-of-the-terraform-execution-environment-and-add
data "http" "ownip" {
  url = "http://ipv4.icanhazip.com"
}

## Existing SSH Pub key for instance (BYOK)
## make sure you have access to the private key (and don't put it to version control)
resource "aws_key_pair" "ssh_key" {
  key_name = "${var.appid}-keypair"
  public_key = file(var.ssh_pubkey_file)
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-keypair"))
}

## security group for ec2
resource "aws_security_group" "instance_sg" {
  name = "${var.appid}-instance-sg"
  description = "Security group for ${var.appid} instances"
  vpc_id = data.aws_vpc.vpc.id
  ingress {
    # allow echo / ping requests
    from_port = 8
    to_port = -1
    protocol = "icmp"
    cidr_blocks = [
      "10.0.0.0/8"]
  }
  ingress {
    # ingress rule for HTTP communication
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    # ingress rule for HTTPS communication
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    # ingress rule for SSH communication
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "${chomp(data.http.ownip.body)}/32"]
  }
  egress {
    # allow all egress rule
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-instance-sg"))
}

//## Actual EC2 instance
resource "aws_instance" "instance" {
  ami = var.aws_instance_ami
  instance_type = var.aws_instance_type
  iam_instance_profile = var.instance_profile_name
  # now
  vpc_security_group_ids = [
    #   data.aws_security_group.ssh.id
    aws_security_group.instance_sg.id
  ]
  subnet_id = data.aws_subnet.app_onea.id
  key_name = aws_key_pair.ssh_key.key_name
  ## User data is limited to 16 KB, in raw form, before it is base64-encoded.
  ## The size of a string of length n after base64-encoding is ceil(n/3)*4.
  user_data = var.user_data
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-instance"))
  volume_tags = merge(local.tags, var.tags, map("Name", "${var.appid}-volume"))
  lifecycle {
    ignore_changes = [
      ami]
  }
}
