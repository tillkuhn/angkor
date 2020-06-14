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

ariable "aws_ssh_security_group_name" {
  type = string
  description = "Name of the security group that manages ssh access into your instance"
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
  default = "files/mykey.pem.pub"
}
