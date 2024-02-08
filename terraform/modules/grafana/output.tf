#output "something" {
#  value = resource.value
#}

# expose to caller
output "service_account_token_viewer_key" {
  value     = grafana_service_account_token.viewer.key
  sensitive = true
}

# expose to caller
#output "cloud_stack" {
#value = data.grafana_cloud_stack.current.url
#}
