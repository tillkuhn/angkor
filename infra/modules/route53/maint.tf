## fails if stopped https://github.com/hashicorp/terraform/issues/1579
## Route 53 ALB Alias for ssh access to compute instance
## https://github.com/terraform-providers/terraform-provider-aws/issues/173
resource "aws_route53_record" "record" {
  zone_id = var.hosted_zone_id
  name = var.domain_name
  ## fully qualified
  type = "A"
  records = [var.public_ip]
  ttl = var.ttl
}
