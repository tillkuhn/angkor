locals {

}

# The resource random_pet generates random pet names that are intended
# to be used as unique identifiers for other resources.
resource "random_pet" "api_token_metrics" {
  keepers = {
    default = var.keeper
  }
}


resource "random_string" "api_token_metrics" {
  keepers = {
    default = var.keeper
  }
  length = 8
}
