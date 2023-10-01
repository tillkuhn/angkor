locals {
  env_id       = var.env_id
  cluster_name = var.app_id
  aws_region   = "eu-central-1"
  # https://docs.confluent.io/cloud/current/clusters/regions.html#az-long-az-regions
  #azure_region        = "germanywestcentral"
  vault_kv_admin_path = "kv/tsc/confluent" # Vault KV path for platform admin stuf
  cloud               = "AWS"              # AWS or AZURE
  #name_suffix = "${var.name_suffix != "" ? "-" : ""}${var.name_suffix}"
}

resource "confluent_environment" "default" {
  display_name = local.env_id
}

resource "confluent_service_account" "env_manager" {
  display_name = "${local.env_id}-env-manager"
  description  = "Service account to manage ${local.env_id} environment"
}

resource "confluent_role_binding" "env_manager_environment_admin" {
  principal   = "User:${confluent_service_account.env_manager.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_environment.default.resource_name
}

# align with https://git.signintra.com/bdp/confluent/kafka-cluster
# registry should use the same cloud provider and region and the main cluster
data "confluent_schema_registry_region" "package" {
  cloud = local.cloud
  # check available regions here, germanywestcentral is currently not available:
  # https://docs.confluent.io/cloud/current/stream-governance/packages.html#stream-governance-regions
  region  = local.aws_region
  package = "ESSENTIALS"
}

resource "confluent_schema_registry_cluster" "main" {
  package = data.confluent_schema_registry_region.package.package

  environment {
    id = confluent_environment.default.id
  }

  region {
    id = data.confluent_schema_registry_region.package.id
  }
}

resource "confluent_api_key" "env_manager_cloud_api_key" {
  display_name = "${local.env_id}-env-manager-cloud-api-key"
  description  = "Cloud API Key that is owned by '${confluent_service_account.env_manager.display_name}' service account"
  owner {
    id          = confluent_service_account.env_manager.id
    api_version = confluent_service_account.env_manager.api_version
    kind        = confluent_service_account.env_manager.kind
  }

  lifecycle {
    prevent_destroy = false
  }
}

# TODO store cloud api key in some store for distribution (e.g. vault, AWS SSM ..)
#resource "vault_generic_secret" "schema_registry_info" {
#  path      = "${local.vault_kv_admin_path}/cloud-api-keys/${local.env}/env-admin"
#  data_json = <<EOT
#{
#  "api_key": "${confluent_api_key.env_manager_cloud_api_key.id}",
#  "api_secret": "${confluent_api_key.env_manager_cloud_api_key.secret}",
#  "display_name": "${confluent_api_key.env_manager_cloud_api_key.display_name}"
#}
#EOT
#}

#create a simple free basic azure cluster to play around
resource "confluent_kafka_cluster" "default" {
  display_name = local.cluster_name
  availability = "SINGLE_ZONE"
  cloud        = local.cloud
  region       = local.aws_region
  basic {}
  environment {
    id = confluent_environment.default.id
  }
  lifecycle { prevent_destroy = false } # should be true for prod
}


resource "confluent_api_key" "cluster" {
  display_name = "${confluent_environment.default.display_name}-cluster-manager-kafka-api-key"
  description  = "Kafka API Key to manage ${confluent_kafka_cluster.default.display_name} cluster resources, owned by ${confluent_service_account.env_manager.display_name}' service account"
  owner {
    id          = confluent_service_account.env_manager.id
    api_version = confluent_service_account.env_manager.api_version
    kind        = confluent_service_account.env_manager.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.default.id
    api_version = confluent_kafka_cluster.default.api_version
    kind        = confluent_kafka_cluster.default.kind

    environment {
      id = confluent_environment.default.id
    }
  }

  lifecycle { prevent_destroy = false }
}

# Store API Key / Secrets in new HCP Vault


# Setup new HashiCorp Cloud Platform App Secrets Store"
resource "hcp_vault_secrets_app" "main" {
  app_name    = var.hcp_vault_secrets_app_name
  description = "HCP Secrets Store for ${var.app_id} App"
}

resource "hcp_vault_secrets_secret" "confluent_cluster_api_key_key" {
  app_name     = hcp_vault_secrets_app.main.app_name
  secret_name  = "confluent_cluster_api_key_key"
  secret_value = confluent_api_key.cluster.id
}

resource "hcp_vault_secrets_secret" "confluent_cluster_api_key_secret" {
  app_name     = hcp_vault_secrets_app.main.app_name
  secret_name  = "confluent_cluster_api_key_secret"
  secret_value = confluent_api_key.cluster.secret
}

# useful for  -H "Authorization:Basic <token>" header in combination with Confluent REST API
resource "hcp_vault_secrets_secret" "confluent_producer_basic_auth" {
  app_name     = hcp_vault_secrets_app.main.app_name
  secret_name  = "confluent_producer_basic_auth"
  secret_value = base64encode("${confluent_api_key.cluster.id}:${confluent_api_key.cluster.secret}")
}


resource "hcp_vault_secrets_secret" "confluent_cluster_rest_endpoint" {
  app_name     = hcp_vault_secrets_app.main.app_name
  secret_name  = "confluent_cluster_rest_endpoint"
  secret_value = confluent_kafka_cluster.default.rest_endpoint
}

resource "hcp_vault_secrets_secret" "confluent_cluster_id" {
  app_name     = hcp_vault_secrets_app.main.app_name
  secret_name  = "confluent_cluster_id"
  secret_value = confluent_kafka_cluster.default.id
}


//  base64encode("Hello World")

# create 0-n topics inside the cluster
# https://saturncloud.io/blog/how-to-use-foreach-to-iterate-over-a-list-of-objects-in-terraform-012/
module "kafka_topic" {
  source = "./modules/kafka_topic"
  # In this example, for_each is used with a for expression to transform the list of objects into a map. The for
  # expression iterates over each item in local.topics, and creates a map where the keys are the topic names
  # and the values are the topic objects.
  for_each           = { for t in var.topics : t.name => t }
  name               = each.key
  retention_hours    = each.value.retention_hours
  partitions_count   = each.value.partitions_count
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
}
