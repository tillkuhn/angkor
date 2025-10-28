locals {

}

# go!

# Setup new HashiCorp Cloud Platform App Secrets Store"
# resource "hcp_vault_secrets_app" "main" {
#   app_name    = var.vault_secrets_app_name
#   description = var.vault_secrets_app_description
# }
#
# # Create happy little secrets
# resource "hcp_vault_secrets_secret" "secret" {
#   for_each     = { for t in var.secrets : t.name => t }
#   app_name     = hcp_vault_secrets_app.main.app_name
#   secret_name  = var.upper_key ? upper(each.key) : each.key
#   secret_value = sensitive(each.value.value)
# }


resource "phase_secret" "secret" {
  for_each = { for t in var.secrets : t.name => t }
  app_id   = var.app_id
  env      = var.env
  key      = var.upper_key ? upper(each.key) : each.key
  path     = var.path
  comment  = "created by tofu"
  tags     = ["generated", "tofu"] // Tags must be pre-created in the Phase Console
  #value   = "postgres://${USER}:${PASSWORD}@${HOST}:{PORT}/${DATABASE}"
  value = sensitive(each.value.value)
}
