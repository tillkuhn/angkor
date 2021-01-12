// module inspired by https://github.com/Flaconi/terraform-aws-iam-ses-user/blob/master/variables.tf
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
