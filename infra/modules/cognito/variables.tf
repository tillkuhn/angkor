variable "appid" {
  type = string
  description = "Application ID"
}

variable "tags" {
  type = map
  description = "Tags to attached to the table, Name tag will be added by the module"
  default = {}
}

variable "allow_admin_create_user_only" {
  description = "Set to True if only the administrator is allowed to create user profiles. Set to False if users can sign themselves up via an app"
  default = true
}

variable "server_side_token_check" {
  description = "Whether server-side token validation is enabled for the identity provider’s token or not."
  default = false
}

variable "callback_urls" {
  type = list
  description = "(Optional) List of allowed callback URLs for the identity providers."
}
