terraform {
  required_version = "~>1.3"
  required_providers {
    confluent = {
      source = "confluentinc/confluent"
    version = "~>2.51" }
  }
}
