#####################################################################
# Configure IAM and Cognito User pools
#####################################################################
## Create COGNITO USER POOL see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool.html
resource "aws_cognito_user_pool" "main" {
  name = var.appid
  auto_verified_attributes = ["email"]
  admin_create_user_config {
    ## set to false so user can register themselves, we still need more authorization to allow this :-)
    allow_admin_create_user_only = var.allow_admin_create_user_only
    invite_message_template {
      email_subject = "Your ${var.appid} temporary password"
      email_message = "Welcome to ${var.appid}! Your username is {username} and temporary password is {####}."
      sms_message = "Your username is {username} and temporary password is {####}."
    }
  }
  email_verification_subject = "Your ${var.appid} verification code"
  tags = merge(var.tags, map("Name", "${var.appid}-user-pool"))
}

# Create COGNITO USER POOL CLIENT for the user pool see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool_client.html
resource "aws_cognito_user_pool_client" "main" {
  name = "webapp"
  generate_secret = false
  explicit_auth_flows = ["USER_PASSWORD_AUTH"]
  user_pool_id = "${aws_cognito_user_pool.main.id}"
}

# Create COGNITO IDENTITY pool and attach the user pool and user pool client id to the identity pool
resource "aws_cognito_identity_pool" "main" {
  identity_pool_name = var.appid
  allow_unauthenticated_identities = true
  cognito_identity_providers {
    client_id               = "${aws_cognito_user_pool_client.main.id}"
    provider_name           = "cognito-idp.${data.aws_region.current.name}.amazonaws.com/${aws_cognito_user_pool.main.id}"
    server_side_token_check = var.server_side_token_check
  }
}
