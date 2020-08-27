#####################################################################
# Configure IAM and Cognito User pools
#####################################################################
## Create COGNITO USER POOL see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool.html
locals {
  tags = map("terraformModule", "cognito")
}

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
  tags = merge(var.tags,local.tags, map("Name", "${var.appid}-user-pool"))
}


# Create COGNITO USER POOL CLIENT for the user pool see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool_client.html
resource "aws_cognito_user_pool_client" "main" {
  name = var.app_client_name != "" ? var.app_client_name : var.appid
  generate_secret = true
  explicit_auth_flows = ["USER_PASSWORD_AUTH"]
  user_pool_id = aws_cognito_user_pool.main.id
  callback_urls = var.callback_urls
  allowed_oauth_flows = ["code"] # also implicit, client_credentials
  allowed_oauth_flows_user_pool_client = true # https://forums.aws.amazon.com/message.jspa?messageID=888870
  allowed_oauth_scopes = ["email", "openid", "profile", "aws.cognito.signin.user.admin"]
  supported_identity_providers = [aws_cognito_identity_provider.facebook_provider.provider_name,"COGNITO"]
}

resource "aws_cognito_identity_provider" "facebook_provider" {
  user_pool_id  = aws_cognito_user_pool.main.id
  provider_name = "Facebook"
  provider_type = "Facebook"

  provider_details = {
    authorize_scopes = "public_profile,email"
    client_id        = var.fb_provider_client_id
    client_secret    = var.fb_provider_client_secret
  }

  # 1) https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_CreateIdentityProvider.html#CognitoUserPools-CreateIdentityProvider-request-AttributeMapping
  # 2) https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-specifying-attribute-mapping.html
  # 3) https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html
  # 4) https://developers.facebook.com/docs/messenger-platform/identity/user-profile/#fields
  #   A mapping of identity provider attributes to standard and custom user pool attributes.
  attribute_mapping = {
    # cognito as per link 3) = fb attribute as per link 4)
    email    = "email"
    username = "id"
    given_name = "first_name"
    family_name = "last_name"
    picture = "profile_pic"
    name = "name"
    # username = "sub"
  }
}

// .auth.eu-central-1.amazoncognito.com
resource "aws_cognito_user_pool_domain" "main" {
  domain       = var.auth_domain_prefix != "" ? var.auth_domain_prefix : var.appid
  user_pool_id = aws_cognito_user_pool.main.id
}

# DO we need this??
# Create COGNITO IDENTITY pool and attach the user pool and user pool client id to the identity pool
resource "aws_cognito_identity_pool" "main" {
  identity_pool_name = var.appid
  allow_unauthenticated_identities = true
  cognito_identity_providers {
    client_id               = aws_cognito_user_pool_client.main.id
    provider_name           = "cognito-idp.${data.aws_region.current.name}.amazonaws.com/${aws_cognito_user_pool.main.id}"
    server_side_token_check = var.server_side_token_check
  }
}
