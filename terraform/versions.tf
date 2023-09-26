# About Constraints: https://www.terraform.io/docs/language/expressions/version-constraints.html
#
# ~>: "the pessimistic constraint operator." Allows only the rightmost version component to increment. 
#     Example: to allow new patch releases within a specific minor release, use the full version number:
#     ~> 1.0.4 will allow install 1.0.5 and 1.0.10 but not 1.1.0.
#     ~> 2.2 will allow install 2.3.x and 2.4.x but not 3.0.x
#
# >, >=, <, <=: Comparisons against a specified version, allowing versions for which the comparison
#     is true. "Greater-than" requests newer versions, and "less-than" requests older versions.
#     Example: version = ">= 1.2.0, < 2.0.0"
terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      # Check at https://registry.terraform.io/providers/hashicorp/aws/latest
      version = "~> 5.13"
    }
    local = {
      source = "hashicorp/local"
      # Check at https://registry.terraform.io/providers/hashicorp/local/latest
      version = "~> 2.3"
    }
    http = {
      source = "hashicorp/http"
      # Check at https://registry.terraform.io/providers/hashicorp/http/latest
      version = "~> 3.2"
    }
    confluent = {
      # Check at https://registry.terraform.io/providers/confluentinc/confluent/latest/docs
      source  = "confluentinc/confluent"
      version = "~> 1.53"
    }
    hcp = {
      source  = "hashicorp/hcp"
      version = "~> 0.71.1"
    }
  }
  # version of terraform itself
  # make sure to align expected version with .terraform-version and github workflow 'infra'
  required_version = "~> 1.3"
}
