variable "appid" {
  type        = string
  description = "Application ID"
}

variable "tags" {
  type        = map(any)
  description = "Common tags to attached to resources, specific ones may be added by the module"
  default     = {}
}

variable "allow_admin_create_user_only" {
  description = "Set to True if only the administrator is allowed to create user profiles. Set to False if users can sign themselves up via an app"
  default     = true
}

variable "server_side_token_check" {
  description = "Whether server-side token validation is enabled for the identity provider’s token or not."
  default     = false
}

variable "callback_urls" {
  type        = list(any)
  description = "(Optional) List of allowed callback URLs for the identity providers."
}

variable "app_client_name" {
  default     = ""
  description = "defaults to appid if empty. app clients will be given a unique ID and an optional secret key to access this user pool."
}

variable "auth_domain_prefix" {
  default     = ""
  description = "Defaultss to appid. Type a domain prefix to use for the sign-up and sign-in pages that are hosted by Amazon Cognito. The prefix must be unique across the selected AWS Region. Domain names can only contain lower-case letters, numbers, and hyphens."
}

variable "fb_provider_client_id" {
  description = "client id as per https://developers.facebook.com/apps/"
}
variable "fb_provider_client_secret" {
  description = "client secret as per https://developers.facebook.com/apps/"
}

# provider versions get deprecated from time to time, check minimum version here (or use later version + dropdown)
# https://developers.facebook.com/docs/graph-api/reference/v8.0/user
# Available Versions: https://developers.facebook.com/docs/graph-api/changelog/
variable "fb_provider_version" {
  description = "facebook provider api version e.g. v9.0"
  default     = "v10.0" # 9.0 expires 23. Feb 2023
}
