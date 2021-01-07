variable "appid" {
  type = string
  description = "Application ID"
}

variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}

variable "aws_vpc_name" {
  type = string
  description = "Name tag of your vpc"
}

variable "aws_subnet_name" {
  type = string
  description = "Name tag of your subnet"
}

variable "aws_instance_type" {
  type = string
  description = "Type of the EC2 instance"
  //  T4g instances are the next generation low cost burstable general purpose instance type that provide a baseline
  // level of CPU performance with the ability to burst CPU usage at any time for as long as required.
  // but https://github.com/aws/aws-cdk/issues/12279 :-(
  default = "t3a.nano"
  #default = "t4g.nano"
}

## Amazon Linux 2 AMI (HVM), SSD Volume Type (64-bit x86)
variable "aws_instance_ami_names" {
  type = list(string)
  # make sure suffix matches processor architecture, e.g. arm64-gp2 for t4g and x86_64-gp2 for t3a
  #default = ["amzn2-ami-hvm*arm64-gp2"]
  default = ["amzn2-ami-hvm*x86_64-gp2"]
  ## aws linux
  #default = "ami-07e308cdb030da01e" ## https://coreos.com/os/docs/latest/booting-on-ec2.html
}

variable "aws_instance_ami_owners" {
  type = list(string)
  default = ["amazon"]
}

variable "ssh_pubkey_file" {
  type = string
  description = "The path to the ssh pub key"
  //default = "../${appid}.pem.pub"
}


variable "user_data" {
  type = string
  description = "The user data script"
}

variable "instance_profile_name" {
  type = string
  description = "The name of the IAM instance profile"
}


# webhook
variable "webhook_port" {
  type        = string
  description = "listener port for webhook"
  default     = 5000
}

variable "stage" {
  type = string
  default = "prod"
  description = "Application stage e.g. prod, dev"
}

