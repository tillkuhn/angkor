# don't configure provider in modules (e.g. credentials for hcp),
# only declare what's required here and use relaxed version ranges
# since callers of the module may run different versions
terraform {
  required_version = "~>1.3"
  required_providers {
    phase = {
      # need to use prefix since phase provider is not on registry.opentofu.or
      source  = "registry.terraform.io/phasehq/phase"
      version = "~> 0.2" // Use the latest appropriate version
    }
    #    hcp = {
    #      source  = "hashicorp/hcp"
    #      version = "~> 0.71"
    #    }
  }
}
