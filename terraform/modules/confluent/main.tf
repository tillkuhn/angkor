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



# just an example how to use modules, so you can share logic and code between environments"
#module "kafka_topic" {
#  topic_name         = "public.hello"
#  source             = "../../modules/kafka_topic"
#  cluster_api_key    = confluent_api_key.cluster.id
#  cluster_api_secret = confluent_api_key.cluster.secret
#  cluster_id         = confluent_kafka_cluster.basic.id
#  cluster_endpoint   = confluent_kafka_cluster.basic.rest_endpoint
#}
#
