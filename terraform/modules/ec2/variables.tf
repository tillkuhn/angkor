variable "appid" {
  type        = string
  description = "Application ID"
}

variable "tags" {
  type        = map(string)
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
  # @see https://aws.amazon.com/de/blogs/aws/new-t4g-instances-burstable-performance-powered-by-aws-graviton2/
  # @see https://instances.vantage.sh/aws/ec2/t4g.micro?currency=USD&region=eu-central-1
  # t4g.nano:  0.5 GiB, 2 vCPU,  5% baseline performance w/  6 credits/hour
  # t4g.micro: 1.0 GiB, 2 vCPU, 10% baseline performance w/ 12 credits/hour
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

variable "ebs_root_volume_size" {
  type    = number
  default = 10
  # Filesystem        Size  Used Avail Use% Mounted on
  # /dev/nvme0n1p1     10G    4,0G  6,0G   41% /
  description = "Size of the EBS root volume in GB, default for t4g.micro is 8 GB but our default is 10 GB (which leaves ~6GB free space after a fresh install)"
  validation {
    condition     = var.ebs_root_volume_size > 0 && var.ebs_root_volume_size <= 100
    error_message = "The EBS root volume size must be a positive number and not larger than 100."
  }
}
