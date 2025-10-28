provider "confluent" {
  cloud_api_key    = var.cloud_api_key
  cloud_api_secret = var.cloud_api_secret
}
terraform {

  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "~>2.51"
    }
  }
}
