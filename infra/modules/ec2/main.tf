# module specific local vars
locals {
  tags = map("terraformModule", "ec2")
}

module "vpcinfo" {
  source = "../vpcinfo"
}

# locate existing vpc by name
data "aws_vpc" "vpc" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_vpc_name]
  }
}

# target subnet for ec2 instance
data "aws_subnet" "app_onea" {
  filter {
    name = "tag:Name"
    values = [
      var.aws_subnet_name]
  }
  vpc_id = data.aws_vpc.vpc.id
}


data "aws_ami" "amazon-linux-2" {
  most_recent = true
  owners = var.aws_instance_ami_owners
  filter {
    name   = "name"
    values = var.aws_instance_ami_names
  }
}

# Existing SSH Pub key for instance (BYOK)
# make sure you have access to the private key (and don't put it to version control)
resource "aws_key_pair" "ssh_key" {
  key_name = "${var.appid}-keypair"
  public_key = file(var.ssh_pubkey_file)
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-keypair"))
}

# security group for ec2
resource "aws_security_group" "instance_sg" {
  name = "${var.appid}-instance-sg"
  description = "Security group for ${var.appid} instances"
  vpc_id = data.aws_vpc.vpc.id
  ingress {
    description = "allow echo / ping requests"
    from_port = 8
    to_port = -1
    protocol = "icmp"
    cidr_blocks = [
      "10.0.0.0/8"]
  }
  ingress {
    description = "ingress rule for HTTP communication (certbot only)"
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for HTTPS nginx communication"
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for HTTP(S) python webhook"
    from_port = var.webhook_port
    to_port = var.webhook_port
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for SSH communication"
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "${module.vpcinfo.ownip}/32"]
  }
  egress {
    description = "allow all egress rule"
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-instance-sg"))
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip_association
# Provides an AWS EIP Association as a top level resource, to associate and
# disassociate Elastic IPs from AWS Instances and Network Interfaces.
resource "aws_eip_association" "eip_assoc" {
  instance_id   = aws_instance.instance.id
  allocation_id = aws_eip.instance_ip.id
}

# also create a random API token which we can share will other services for internal API communication
resource "random_uuid" "api_token" {
  keepers = {
    ami_id = data.aws_ami.amazon-linux-2.id
  }
}

# Actual EC2 instance
resource "aws_instance" "instance" {
  # Read the AMI id "through" the random_pet resource to ensure that
  # both will change together.
  ami = random_uuid.api_token.keepers.ami_id
  instance_type = var.aws_instance_type
  iam_instance_profile = var.instance_profile_name
  vpc_security_group_ids = [
    aws_security_group.instance_sg.id
  ]
  subnet_id = data.aws_subnet.app_onea.id
  key_name = aws_key_pair.ssh_key.key_name
  # User data is limited to 16 KB, in raw form, before it is base64-encoded.
  # The size of a string of length n after base64-encoding is ceil(n/3)*4.
  user_data = var.user_data
  tags = merge(local.tags, var.tags, map("Name", "${var.appid}-${lookup(var.tags, "releaseName", "default")}","stage",var.stage))
  volume_tags = merge(local.tags, var.tags, map("Name", "${var.appid}-volume"))
  # remove this block if you want to always want to recreate instance if a new AMI arrives
  lifecycle {
    ignore_changes = [ami]
  }
}

resource "aws_eip" "instance_ip" {
  vpc      = true
  tags     = local.tags
}

