output "account_id" {
  value = data.aws_caller_identity.current.account_id
}

output "vpc_id" {
  value = data.aws_vpc.vpc.id
}

output "aws_region" {
  value = data.aws_region.current.name
}
