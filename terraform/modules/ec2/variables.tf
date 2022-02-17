variable "appid" {
  type        = string
  description = "Application ID"
}

variable "tags" {
  type        = map(any)
  description = "Tags to attached to the table, Name tag will be added by the module"
  default     = {}
}

variable "aws_vpc_name" {
  type        = string
  description = "Name tag of your vpc"
}

variable "aws_subnet_name" {
  type        = string
  description = "Name tag of your subnet"
}

variable "aws_instance_type" {
  type        = string
  description = "Type of the EC2 instance"
  # T4g instances are the next generation low cost burstable general purpose instance type that provide a baseline
  # level of CPU performance with the ability to burst CPU usage at any time for as long as required.
  # https://aws.amazon.com/de/blogs/aws/new-t4g-instances-burstable-performance-powered-by-aws-graviton2/
  # t4g.nano: 0.5 GiB, t4g.micro: 1 GiB
  default = "t4g.micro"
}

## Amazon Linux 2 AMI (HVM), SSD Volume Type (64-bit x86)
variable "aws_instance_ami_names" {
  type = list(string)
  # make sure suffix matches processor architecture (arm64 versus x86_64)
  # e.g. amzn2-ami-hvm*arm64-gp2 for t4g with Arm-based AWS Graviton2 processor
  #      amzn2-ami-hvm*x86_64-gp2 for "classically" equipped t3a instances
  default = ["amzn2-ami-hvm*arm64-gp2"]
}

variable "aws_instance_ami_owners" {
  type    = list(string)
  default = ["amazon"]
}

variable "ssh_pubkey_file" {
  type        = string
  description = "The path to the ssh pub key"
  //default = "../${appid}.pem.pub"
}


variable "user_data" {
  type        = string
  description = "The user data script"
}

variable "instance_profile_name" {
  type        = string
  description = "The name of the IAM instance profile"
}


# webhook
variable "webhook_port" {
  type        = string
  description = "listener port for webhook"
  default     = 5000
}

variable "stage" {
  type        = string
  default     = "prod"
  description = "Application stage e.g. prod, dev"
}
