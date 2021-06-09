# Cognito groups with associated IAM Roles
# https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-user-groups.html

# "guest" - known but no significant edit privileges
resource "aws_iam_role" "guest_role" {
  name               = "${var.appid}-cognito-role-guest"
  tags               = merge(local.tags, var.tags)
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity"
    }
  ]
}
EOF
}

resource "aws_cognito_user_group" "guest" {
  name         = "${var.appid}-guests"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "${var.appid} guest users"
  role_arn     = aws_iam_role.guest_role.arn
  precedence   = 64
}


# "ordinary" user
resource "aws_iam_role" "user_role" {
  name               = "${var.appid}-cognito-role-user"
  tags               = merge(local.tags, var.tags)
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity"
    }
  ]
}
EOF
}

resource "aws_cognito_user_group" "user" {
  name         = "${var.appid}-users"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "${var.appid} normal users"
  role_arn     = aws_iam_role.user_role.arn
  # You can assign precedence values to each group. The group with the better (lower) precedence
  # will be chosen and its associated IAM role will be applied.
  precedence = 42
}


# admin can do almost everything
resource "aws_iam_role" "admin_role" {
  name               = "${var.appid}-cognito-role-admin"
  tags               = merge(local.tags, var.tags)
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity"
    }
  ]
}
EOF
}

resource "aws_cognito_user_group" "admin" {
  name         = "${var.appid}-admins"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "${var.appid} admin users"
  role_arn     = aws_iam_role.admin_role.arn
  precedence   = 24
}

