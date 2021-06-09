# About Constraints: https://www.terraform.io/docs/language/expressions/version-constraints.html
#
# ~>: "the pessimistic constraint operator." Allows only the rightmost version component to increment. 
#     Example: to allow new patch releases within a specific minor release, use the full version number:
#     ~> 1.0.4 will allow  install 1.0.5 and 1.0.10 but not 1.1.0. 
#
# >, >=, <, <=: Comparisons against a specified version, allowing versions for which the comparison
#     is true. "Greater-than" requests newer versions, and "less-than" requests older versions.
#     Example: version = ">= 1.2.0, < 2.0.0"
terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      # Check at https://registry.terraform.io/providers/hashicorp/aws/latest
      version = ">= 3.22.0, < 4.0.0"
    }
    local = {
      source = "hashicorp/local"
      # Check at https://registry.terraform.io/providers/hashicorp/local/latest
      version = ">= 2.0.0, < 3.0.0"
    }
    http = {
      source = "hashicorp/http"
      # check at https://registry.terraform.io/providers/hashicorp/http/latest
      version = ">= 2.0.0, < 3.0.0"
    }
  }
  # terraform itself. if patch version not specified, it will always use latest (e.g. 1.14.7 if >= 0.14)
  # make sure to align expected version with .terraform-version and github workflow 'infra'
  #required_version = ">= 0.14, < 0.15"
  required_version = ">= 1.0.0"
}
