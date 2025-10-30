# output "organization_id" {
#   #value = hcp_vault_secrets_app.main.organization_id
# }
#
# output "project_id" {
#   #value = hcp_vault_secrets_app.main.project_id
# }
#

output "secrets" {
  value = data.phase_secrets.main.secrets
}
