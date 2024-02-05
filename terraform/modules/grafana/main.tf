# url and credentials must be passed to the module
provider "grafana" {
  url  = var.url
  auth = var.auth
}

// Create resources (optional: within the organization)
resource "grafana_folder" "my_folder" {
  #org_id = grafana_organization.my_org.org_id # not for Grafana Cloud !
  title = "Terraform Folder"
}

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
