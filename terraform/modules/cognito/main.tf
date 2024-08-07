#####################################################################
# Configure IAM and Cognito User pools
#####################################################################
## Create COGNITO USER POOL see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool.html
locals {
  tags = tomap({ "terraformModule" = "cognito" })
}

resource "aws_cognito_user_pool" "main" {
  name = var.appid
  auto_verified_attributes = [
    "email"
  ]
  admin_create_user_config {
    ## set to false so user can register themselves, we still need more authorization to allow this :-)
    allow_admin_create_user_only = var.allow_admin_create_user_only
    invite_message_template {
      email_subject = "Your ${var.appid} temporary password"
      email_message = "Welcome to ${var.appid}! Your username is {username} and temporary password is {####}."
      sms_message   = "Your username is {username} and temporary password is {####}."
    }
  }
  email_verification_subject = "Your ${var.appid} verification code"
  tags                       = merge(var.tags, local.tags, tomap({ "Name" = "${var.appid}-user-pool" }))
}


# Create COGNITO USER POOL CLIENT for the user pool see https://www.terraform.io/docs/providers/aws/r/cognito_user_pool_client.html
resource "aws_cognito_user_pool_client" "main" {
  name            = var.app_client_name != "" ? var.app_client_name : var.appid
  generate_secret = true
  # https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_CreateUserPoolClient.html#CognitoUserPools-CreateUserPoolClient-request-ExplicitAuthFlows
  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]
  user_pool_id  = aws_cognito_user_pool.main.id
  callback_urls = var.callback_urls
  allowed_oauth_flows = [
    "code"
  ]
  # also implicit, client_credentials
  allowed_oauth_flows_user_pool_client = true
  # https://forums.aws.amazon.com/message.jspa?messageID=888870
  allowed_oauth_scopes = [
    "email",
    "openid",
    "profile",
    "aws.cognito.signin.user.admin"
  ]
  supported_identity_providers = [
    aws_cognito_identity_provider.facebook_provider.provider_name,
    aws_cognito_identity_provider.google_provider.provider_name,
    "COGNITO"
  ]
  # Time limit, between 5 minutes and 1 day, after which the access token is no longer valid and cannot be used.
  access_token_validity = 24
  # Time limit, between 5 minutes and 1 day, after which the ID token is no longer valid and cannot be used.
  id_token_validity = 24
  # Time limit in days refresh tokens are valid for. Must be between 60 minutes and 3650 days
  refresh_token_validity = 7
  # Released with 3.32.0 of the TF AWS Provider https://github.com/hashicorp/terraform-provider-aws/issues/14919
  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }

}

# In Addition to Cognito User Pool, add FB as external Provider
resource "aws_cognito_identity_provider" "facebook_provider" {
  user_pool_id  = aws_cognito_user_pool.main.id
  provider_name = "Facebook"
  provider_type = "Facebook"

  provider_details = {
    authorize_scopes = "public_profile,email"
    client_id        = var.fb_provider_client_id
    client_secret    = var.fb_provider_client_secret
    # fb provider api version  needs to be updated from time to time when
    # old versions become unsupported, see variables.tf for links and details
    api_version                   = var.fb_provider_version
    attributes_url                = "https://graph.facebook.com/${var.fb_provider_version}/me?fields="
    attributes_url_add_attributes = "true"
    authorize_url                 = "https://www.facebook.com/${var.fb_provider_version}/dialog/oauth"
    token_request_method          = "GET"
    token_url                     = "https://graph.facebook.com/${var.fb_provider_version}/oauth/access_token"

  }

  # 1) https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_CreateIdentityProvider.html#CognitoUserPools-CreateIdentityProvider-request-AttributeMapping
  # 2) https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-specifying-attribute-mapping.html
  # 3) https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html
  # 4) https://developers.facebook.com/docs/messenger-platform/identity/user-profile/#fields
  #   A mapping of identity provider attributes to standard and custom user pool attributes.
  attribute_mapping = {
    # cognito as per link 3) = fb attribute as per link 4)
    email       = "email"
    username    = "id"
    given_name  = "first_name"
    family_name = "last_name"
    #  picture = "profile_pic"
    name = "name"
    # username = "sub"
  }
}

# NEW Alternative Google Provider, "Setting up Google as an identity pool IdP"
# https://docs.aws.amazon.com/cognito/latest/developerguide/google.html
# * OAuth Consent Screen https://console.cloud.google.com/apis/credentials/consent?project=<your-project>
# * Credentials OAuth 2.0 Client IDs, e.g. angkor-google-client Type Web Application
# * Copy Client ID e.g. 81*********-8********.apps.googleusercontent.com
# * App Integration
# To make Google Available in Web UI You Have to
# App client: <your client>  -> Edit Hosted UI -> Identity Providers -> Add Google (along with FB and Cognito User Pool)
# In Addition to Cognito User Pool, add FB as external Provider
resource "aws_cognito_identity_provider" "google_provider" {
  user_pool_id  = aws_cognito_user_pool.main.id
  provider_name = "Google"
  provider_type = "Google"

  provider_details = {
    authorize_scopes = "profile email openid" # profile (not public_profile), separated by spaces (not comma)
    client_id        = var.google_provider_client_id
    client_secret    = var.google_provider_client_secret

    # need to set the following attributes as well (see the values after the 1st run) to prevent a diff on each tf run
    "attributes_url"                = "https://people.googleapis.com/v1/people/me?personFields="
    "attributes_url_add_attributes" = "true"
    "authorize_url"                 = "https://accounts.google.com/o/oauth2/v2/auth"
    "oidc_issuer"                   = "https://accounts.google.com"
    "token_request_method"          = "POST"
    "token_url"                     = "https://www.googleapis.com/oauth2/v4/token"

  }

  attribute_mapping = {
    # cognito as per link 3) = fb attribute as per link 4)
    email       = "email"
    username    = "sub"         # FB: id
    given_name  = "given_name"  # FB: first_name
    family_name = "family_name" # FB: last_name
    #  picture = "profile_pic"
    name = "name"
  }
}


// my-domain.auth.eu-central-1.amazoncognito.com
resource "aws_cognito_user_pool_domain" "main" {
  domain       = var.auth_domain_prefix != "" ? var.auth_domain_prefix : var.appid
  user_pool_id = aws_cognito_user_pool.main.id
}

// resource server required to introduce custom scopes (used for cli app client)
// Scopes must adhere to the following naming convention: {application_name}.{scope}.
resource "aws_cognito_resource_server" "main" {
  user_pool_id = aws_cognito_user_pool.main.id

  identifier = "${var.appid}-resources"
  name       = "${var.appid} resources"

  scope {
    scope_name        = "read"
    scope_description = "Scope which allows read access to resources"
  }

  scope {
    scope_name        = "write"
    scope_description = "Scope which allows full access to resources"
  }

  scope {
    scope_name        = "delete"
    scope_description = "Dedicated Scope that grants permissions to delete resources"
  }

  scope {
    scope_name        = "admin"
    scope_description = "Administer resources, allows any action"
  }

}

# Create an additional CLI client for resource server
# "Server to Server Auth with Amazon Cognito"
# https://lobster1234.github.io/2018/05/31/server-to-server-auth-with-amazon-cognito/
resource "aws_cognito_user_pool_client" "cli" {
  name            = "${var.appid}-cli"
  user_pool_id    = aws_cognito_user_pool.main.id
  generate_secret = true
  # allowed values: implicit, client_credentials
  allowed_oauth_flows = [
    "client_credentials"
  ]
  # "Re: OAuth is not enabled for app client settings modified via API"
  # https://forums.aws.amazon.com/message.jspa?messageID=888870
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes = [
    "${aws_cognito_resource_server.main.identifier}/read",
    "${aws_cognito_resource_server.main.identifier}/write",
    "${aws_cognito_resource_server.main.identifier}/delete",
    "${aws_cognito_resource_server.main.identifier}/admin",
  ]
  # should be short lived (here: 1h)
  access_token_validity  = 1
  id_token_validity      = 1
  refresh_token_validity = 1
  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }

}
