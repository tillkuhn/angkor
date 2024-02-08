# don't configure provider in modules (e.g. credentials for hcp),
# only declare what's required here and use relaxed version ranges
# since callers of the module may run different versions
terraform {
  required_version = "~>1.5"
  required_providers {
    #    hcp = {
    #      source  = "hashicorp/hcp"
    #      version = "~> 0.71"
    #    }
  }
}
