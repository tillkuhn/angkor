data "aws_caller_identity" "current" {}

data "aws_vpcs" "current" {}

data "aws_region" "current" {}

data "aws_vpc" "vpc" {
  id = local.vpc_id
}

# Find my own ip to include it in ssh security group
# https://stackoverflow.com/questions/46763287/i-want-to-identify-the-public-ip-of-the-terraform-execution-environment-and-add
# https://registry.terraform.io/providers/hashicorp/http/latest/docs/data-sources/http
#  At present this resource can only retrieve data from URLs that respond with text/* or application/json content types,
data "http" "ownip" {
  # url = "http://ipv4.icanhazip.com" # suddenly returned ipv6 instead of ipv4
  # url = "https://checkip.amazonaws.com/" # no Content-Type header :-(
  url = "http://whatismyip.akamai.com/" # works ... for the time being
}

locals {
  // assume only one vpc, so we take the first
  vpc_id = element(tolist(data.aws_vpcs.current.ids), 0)
}

