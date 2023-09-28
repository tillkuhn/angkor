# providers.tf Terraform Provider Configuration for main module

# see terraform-backend.tf.tmpl and remove extension
# to enable s3 backend for remote shared terraform state
provider "aws" {
  region = "eu-central-1"
}

# https://registry.terraform.io/providers/hashicorp/hcp/latest/docs
# https://registry.terraform.io/providers/hashicorp/hcp/latest/docs/guides/auth
# The HCP (HashiCorp Cloud Platform) provider accepts two forms of authentication:
# * client credentials, obtained on the creation of a service principal key
# * user session, obtained via browser login (as of v0.45.0)
#
# The client_id and client_secret must come from a service principal key. Service principals and service principal keys
# can be created in the HCP portal with an existing user account. The service principal must be authorized to access the
# API. Initially, it has no permissions, so the IAM policy must be updated to grant it permissions.
#
# HCP has two types of Service Principals. Organization-Level Service Principals and Project-Level Service Principals.
# Either can be used with the HCP Terraform Provider. To read more about their differences please see our documentation page.
#
# CAUTION: If a Project-Level Service Principal is used, specify the default project_id in your provider configuration.
# Viewer Can only view existing resources.
# Contributor: Can create and manage all types of resources but can't grant access to others.
# Admin Has full access to all resources including the right to edit IAM, invite users, edit roles.

provider "hcp" {
  client_id     = var.hcp_client_id     #var.HCP_CLIENT_ID
  client_secret = var.hcp_client_secret #var.HCP_CLIENT_SECRET
}
