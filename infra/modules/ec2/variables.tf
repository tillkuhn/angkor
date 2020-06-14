
variable "appid" {
  type = string
  description = "Application ID"
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
variable "aws_instance_ami" {
  type = string
  default = "ami-0f3a43fbf2d3899f7" ## aws linux
  #default = "ami-07e308cdb030da01e" ## https://coreos.com/os/docs/latest/booting-on-ec2.html
}

variable "ssh_pubkey_file" {
  type = string
  description = "The path to the ssh pub key"
  //default = "../${appid}.pem.pub"
}


variable "user_data_template" {
  type = string
  description = "The path to the user data script"
}


variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}
