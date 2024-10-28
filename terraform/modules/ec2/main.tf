# module specific local vars
locals {
  tags = tomap({ "terraformModule" = "ec2" })
  # can be used on combination with resources like random_uuid to  rotate secrets
  time_keeper = formatdate("YYYY", timestamp()) # YYYY = rotate once a year
  ami_keeper  = data.aws_ami.amazon-linux-2.id
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
data "aws_subnet" "app_net" {
  filter {
    name = "tag:Name"
    values = [
    var.aws_subnet_name]
  }
  vpc_id = data.aws_vpc.vpc.id
}


# Example: amzn2-ami-hvm*arm64-gp2 for t4g with Arm-based AWS Graviton2 processor
data "aws_ami" "amazon-linux-2" {
  most_recent = true
  owners      = var.aws_instance_ami_owners
  filter {
    name   = "name"
    values = var.aws_instance_ami_names
  }
}

# existing SSH Pub key for instance ("bring your own key")
# make sure you have access to the private key, and NEVER EVER put it to version control
resource "aws_key_pair" "ssh_key" {
  key_name   = "${var.appid}-keypair"
  public_key = file(var.ssh_pubkey_file)
  tags       = merge(local.tags, var.tags, tomap({ "Name" = "${var.appid}-keypair" }))
}

# security group for ec2
resource "aws_security_group" "instance_sg" {
  name        = "${var.appid}-instance-sg"
  description = "Security group for ${var.appid} instances"
  vpc_id      = data.aws_vpc.vpc.id
  ingress {
    description = "allow echo / ping requests"
    from_port   = 8
    to_port     = -1
    protocol    = "icmp"
    cidr_blocks = [
    "10.0.0.0/8"]
  }
  ingress {
    description = "ingress rule for HTTP communication (certbot only)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [
    "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for HTTPS nginx communication"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [
    "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for HTTP(S) python webhook"
    from_port   = var.webhook_port
    to_port     = var.webhook_port
    protocol    = "tcp"
    cidr_blocks = [
    "0.0.0.0/0"]
  }
  ingress {
    description = "ingress rule for SSH communication"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [
    "${module.vpcinfo.ownip}/32"]
  }
  egress {
    description = "allow all egress rule"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [
    "0.0.0.0/0"]
  }
  tags = merge(local.tags, var.tags, tomap({ "Name" = "${var.appid}-instance-sg" }))
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip_association
# Provides an AWS EIP Association as a top level resource, to associate and
# disassociate Elastic IPs from AWS Instances and Network Interfaces.
resource "aws_eip_association" "eip_assoc" {
  instance_id   = aws_instance.instance.id
  allocation_id = aws_eip.instance_ip.id
}



# Create a random API token which we can share will other services for internal API communication
# Use AMI ID as "keeper" to keep it stable for some time but ensure rotation
resource "random_uuid" "api_token" {
  keepers = {
    # ami_id changes too often which conflicts with the limited number of HCP Vault Secrets
    # ami_id = data.aws_ami.amazon-linux-2.id
    # rotate once per year
    # main = local.time_keeper # change will trigger recreation of resource.
    main = "2024" # change will trigger recreation of resource.
  }
}

# Manage actual EC2 instance.
# The ignore_changes = [ami] ensures that we don't destroy and recreate if the AMI changes
# But we should do so in regular intervals, in a controlled way!
resource "aws_instance" "instance" {
  ami                  = data.aws_ami.amazon-linux-2.id
  instance_type        = var.aws_instance_type
  iam_instance_profile = var.instance_profile_name
  vpc_security_group_ids = [
    aws_security_group.instance_sg.id
  ]
  subnet_id = data.aws_subnet.app_net.id
  key_name  = aws_key_pair.ssh_key.key_name

  # User data is limited to 16 KB, in raw form, before it is base64-encoded.
  # The size of a string of length n after base64-encoding is ceil(n/3)*4.
  user_data                   = var.user_data
  user_data_replace_on_change = true # true means recreate the instance, even on the slightest change in user-data

  tags        = merge(local.tags, var.tags, tomap({ "Name" = "${var.appid}-${lookup(var.tags, "releaseName", "default")}", "stage" = var.stage }))
  volume_tags = merge(local.tags, var.tags, tomap({ "Name" = "${var.appid}-volume" }))

  # Remove / uncomment ignore_changes in the lifecycle block if you want to *RECREATE* the current EC2 instance
  # whenever a new AMI ID is available (which happens every couple of month). Only literal values
  # are allowed for ignore_changes, see https://www.terraform.io/language/meta-arguments/lifecycle#literal-values-only
  lifecycle {
    # ignore_changes = [ami]
  }

}

# Single EIP associated with an instance
resource "aws_eip" "instance_ip" {
  domain = "vpc"
  tags   = local.tags
}

