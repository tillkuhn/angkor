# url and credentials must be passed to the module
provider "grafana" {
  url = var.url
  # String, Sensitive) API token, basic auth in the username:password format or anonymous (string literal).
  # May alternatively be set via the GRAFANA_AUTH environment variable.
  # Why? the Grafana client is required for `grafana_service_account`. Set the auth and url provider attributes
  auth = var.auth
  # String, Sensitive) Access Policy Token (or API key) for Grafana Cloud. May alternatively
  # be set via the GRAFANA_CLOUD_API_KEY environment variable
  #   with module.grafana.provider["registry.opentofu.org/grafana/grafana"],
  #  on modules/grafana/main.tf line 10, in provider "grafana":
  #  10:   cloud_api_key = var.cloud_api_key
  # Use cloud_access_policy_token instead.
  # cloud_api_key = var.cloud_api_key
  cloud_access_policy_token = var.cloud_api_key
}

# Create resources (optional: within the organization)
resource "grafana_folder" "my_folder" {
  #org_id = grafana_organization.my_org.org_id # not for Grafana Cloud !
  title = "Terraform Folder"
}

# Creating a service account in Grafana instance to be used as auth and attach tokens
# notice we can attach multiple tokens to one service account
# permissions https://registry.terraform.io/providers/grafana/grafana/latest/docs/resources/service_account_permission
resource "grafana_service_account" "viewer" {
  name = "viewer-service-account"
  role = "Viewer" # available in UI: Viewer, Editor, Admin, No Basic Role
}

# Creating a service account token in Grafana instance to be used for viewing resources in Grafana instance
resource "grafana_service_account_token" "viewer" {
  name               = "viewer"
  service_account_id = grafana_service_account.viewer.id
}


# this required cloud_api_key on provider with at least stack:read privileges
#data "grafana_cloud_stack" "current" {
#  slug = var.slug
#}

# │ Error: the Cloud API client is required for `grafana_cloud_access_policy`. Set the cloud_api_key provider attribute
# https://grafana.com/docs/grafana-cloud/account-management/cloud-portal/:
# Free accounts are not able to create or delete individual services from a Stack and are limited to one Stack.
#resource "grafana_cloud_access_policy" "hase" {
#  name   = "hase"
#  region = "eu"
#  scopes = ["logs:write"]
#  realm {
#    identifier = ""
#    type       = "stack"
#  }
#}

locals {
  dashboard_title = "Fancy generated dashboard 22"
}

resource "grafana_dashboard" "test_folder" {
  folder = grafana_folder.my_folder.id
  config_json = templatefile("${path.module}/templates/dashboard.json", {
    title        = local.dashboard_title
    panel_title  = "Room Temperature"
    panel_metric = "till_test_heat_metric"
  })
  #config_json = jsonencode({
  #  "title" : "Fancy generated dashboard",
  #  "uid" : "my-dashboard-uid"
  #})
}
