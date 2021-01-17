// module inspired by https://github.com/Flaconi/terraform-aws-iam-ses-user/blob/master/variables.tf
data "aws_region" "current" {}

resource "aws_iam_user" "mailer" {
  name                 = "${var.appid}-mailer"
  path                 = "/"
  // permissions_boundary = var.permissions_boundary
  // force_destroy        = var.force_destroy
  tags                 = var.tags
}

resource "aws_iam_access_key" "mailer" {
  user    = aws_iam_user.mailer.name
 // pgp_key = var.pgp_key
}

data "aws_iam_policy_document" "ses_send_access" {
  statement {
    effect = "Allow"
    actions = [
      "ses:SendRawEmail",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_user_policy" "mailer" {
  name_prefix = "SESSendOnlyAccess"
  user        = aws_iam_user.mailer.name
  policy = data.aws_iam_policy_document.ses_send_access.json
}

# ses domain and DKIM support
resource "aws_ses_domain_identity" "ses_domain" {
  domain = var.domain_name
}

resource "aws_route53_record" "ses_domain_amazonses_verification_record" {
  zone_id = var.hosted_zone_id
  name    = "_amazonses.${var.domain_name}"
  type    = "TXT"
  ttl     = "600"
  records = [aws_ses_domain_identity.ses_domain.verification_token]
}

resource "aws_ses_domain_dkim" "dkim" {
  domain = aws_ses_domain_identity.ses_domain.domain
}

resource "aws_route53_record" "ses_amazonses_dkim_record" {
  count   = 3
  zone_id =  var.hosted_zone_id
  name    = "${element(aws_ses_domain_dkim.dkim.dkim_tokens, count.index)}._domainkey.${var.domain_name}"
  type    = "CNAME"
  ttl     = "600"
  records = ["${element(aws_ses_domain_dkim.dkim.dkim_tokens, count.index)}.dkim.amazonses.com"]
}
