data "aws_caller_identity" "current" {}

data "aws_vpcs" "current" {}

data "aws_region" "current" {}

data "aws_vpc" "vpc" {
  id = local.vpc_id
}

locals {
  // assume only one vpc, so we take the first
  vpc_id = element(tolist(data.aws_vpcs.current.ids), 0)
}

