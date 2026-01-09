# versions.tf - configure terraform and required provider versions
#
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
#
# Preferred for this project: use pessimistic constraint operator (~>) with 2 digit (major.minor) version
terraform {
  # version of terraform itself
  # make sure to align expected version with .terraform-version and github workflow 'infra'
  required_version = "~> 1.3"

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

    # Confluent for Kafka Topics
    # https://registry.terraform.io/providers/confluentinc/confluent/latest/docs
    confluent = {
      # Check at https://registry.terraform.io/providers/confluentinc/confluent/latest/docs
      source  = "confluentinc/confluent"
      version = "~> 2.51"
    }

    # Phase for secret management as of 2025-10
    # https://registry.terraform.io/providers/phasehq/phase/latest/docs
    # https://docs.phase.dev/integrations/platforms/hashicorp-terraform
    # requires  Phase account, and dedicated service account @ https://console.phase.dev/timafe/access/service-accounts
    # manage access for service account (Development, staging, production) and create access token
    # curl --request GET --url 'https://api.phase.dev/v1/secrets/?app_id=${appId}&env=development' --header 'Authorization: Bearer ServiceAccount cae(...)'
    phase = {
      # need to use prefix since phase provider is not on registry.opentofu.or
      source  = "registry.terraform.io/phasehq/phase"
      version = "~> 0.2" // Use the latest appropriate version
    }

    # Grafana resources for Monitoring
    # https://registry.terraform.io/providers/grafana/grafana/latest/docs
    grafana = {
      source  = "grafana/grafana"
      version = "~> 3.7"
    }

    # Neon provider for Postgres on Neon
    # https://neon.com/docs/reference/terraform
    neon = {
      source = "kislerdm/neon"
    }
  }
}
