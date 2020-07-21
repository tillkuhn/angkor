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
  description = "type of the EC2 instance"
  default = "t3a.nano"
}

## Amazon Linux 2 AMI (HVM), SSD Volume Type (64-bit x86)
variable "aws_instance_ami_names" {
  type = list(string)
  default = ["amzn2-ami-hvm*"]
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
