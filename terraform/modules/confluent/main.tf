# For code examples, check
# https://github.com/confluentinc/terraform-provider-confluent/tree/master/examples/configurations
#

locals {
  env_id       = var.env_id
  cluster_name = var.app_id
  # Overview of available regions: https://docs.confluent.io/cloud/current/clusters/regions.html#az-long-az-regions
  aws_region = "eu-central-1"
  cloud      = "AWS" # AWS or AZURE
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

# MANAGE SCHEMA REGISTRY
# After Upgrade to provider version 2: resource confluent_schema_registry_cluster and
# datasource confluent_schema_registry_region have been deprecated
# https://registry.terraform.io/providers/confluentinc/confluent/latest/docs/guides/version-2-upgrade
data "confluent_schema_registry_cluster" "main" {
  environment {
    id = confluent_environment.default.id
  }
}

# API key to access schema registry
resource "confluent_api_key" "env_manager_schema_registry_api_key" {
  display_name = "${local.env_id}-env-manager-schema-registry-api-key"
  description  = "Schema Registry API Key owned by '${confluent_service_account.env_manager.display_name}' service account"
  owner {
    id          = confluent_service_account.env_manager.id
    api_version = confluent_service_account.env_manager.api_version
    kind        = confluent_service_account.env_manager.kind
  }
  managed_resource {
    id          = data.confluent_schema_registry_cluster.main.id
    api_version = data.confluent_schema_registry_cluster.main.api_version
    kind        = data.confluent_schema_registry_cluster.main.kind

    environment {
      id = confluent_environment.default.id
    }
  }
}

# API key to manage the entire environment
resource "confluent_api_key" "env_manager_cloud_api_key" {
  display_name = "${local.env_id}-env-manager-cloud-api-key"
  description  = "Cloud API Key that is owned by '${confluent_service_account.env_manager.display_name}' service account"
  owner {
    id          = confluent_service_account.env_manager.id
    api_version = confluent_service_account.env_manager.api_version
    kind        = confluent_service_account.env_manager.kind
  }

}

# Sone cheap basic cluster should be enough for our little project
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

# API Key for cluster manager
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

# Kafka Topics - create 0-n topics inside the cluster based on the list of topic specs
module "kafka_topic" {
  source = "./modules/kafka_topic"
  # In this example, for_each is used with a for expression to transform the list of objects into a map. The for
  # expression iterates over each item in local.topics,
  # and creates a map where the keys are the topic *names* and the values are the topic *objects*.
  for_each           = { for t in var.topics : t.name => t }
  name               = each.key
  retention_hours    = each.value.retention_hours
  partitions_count   = each.value.partitions_count
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
}


# Attach Cloud Events registry schema for app.events
# https://registry.terraform.io/providers/confluentinc/confluent/latest/docs/resources/confluent_schema
resource "confluent_schema" "app_events" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.main.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.main.rest_endpoint
  subject_name  = "${var.topic_acl_app_prefix}events-value" # - value suffix is essential to match the topic
  format        = "JSON"                                    # "JSON" or "AVRO"
  schema        = file("${path.module}/schemas/cloudevents.json")
  credentials {
    key    = confluent_api_key.env_manager_schema_registry_api_key.id
    secret = confluent_api_key.env_manager_schema_registry_api_key.secret
  }
}

# Let's switch gears and add some consumer / producer ACLs for fine grained permissions, Inspired by

# Loop for Producer Service Accounts
resource "confluent_service_account" "producer" {
  for_each     = var.service_accounts_producer
  display_name = each.value.name != null && each.value.name != "" ? each.value.name : "${each.key}-producer"
  description  = "Service Account for ${each.key} producer"
}

resource "confluent_api_key" "producer" {
  for_each     = var.service_accounts_producer
  display_name = each.value.name != null && each.value.name != "" ? "${each.value.name}-kafka-api-key" : "${each.key}-producer-kafka-api-key"
  description  = "Kafka API Key owned by '${each.key}-producer' service account"
  owner {
    id          = confluent_service_account.producer[each.key].id
    api_version = confluent_service_account.producer[each.key].api_version
    kind        = confluent_service_account.producer[each.key].kind
  }
  managed_resource {
    id          = confluent_kafka_cluster.default.id
    api_version = confluent_kafka_cluster.default.api_version
    kind        = confluent_kafka_cluster.default.kind
    environment {
      id = confluent_environment.default.id
    }
  }
}

# attach topic ACLs for producer service accounts
module "topic_acl_producer" {
  source             = "./modules/topic_acls"
  for_each           = { for sa_key, sa in var.service_accounts_producer : sa_key => sa.acl_prefixes }
  resource_names     = each.value
  principal_id       = confluent_service_account.producer[each.key].id
  operation          = "WRITE"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}


# Loop for Consumer Service Accounts
resource "confluent_service_account" "consumer" {
  for_each     = var.service_accounts_consumer
  display_name = each.value.name != null && each.value.name != "" ? each.value.name : "${each.key}-consumer"
  description  = "Service Account for ${each.key} consumer"
}

resource "confluent_api_key" "consumer" {
  for_each     = var.service_accounts_consumer
  display_name = each.value.name != null && each.value.name != "" ? "${each.value.name}-kafka-api-key" : "${each.key}-consumer-kafka-api-key"
  description  = "Kafka API Key owned by '${each.key}-consumer' service account"
  owner {
    id          = confluent_service_account.consumer[each.key].id
    api_version = confluent_service_account.consumer[each.key].api_version
    kind        = confluent_service_account.consumer[each.key].kind
  }
  managed_resource {
    id          = confluent_kafka_cluster.default.id
    api_version = confluent_kafka_cluster.default.api_version
    kind        = confluent_kafka_cluster.default.kind
    environment {
      id = confluent_environment.default.id
    }
  }
}

# loop to attach TOPIC and GROUP ACLs for each consumer service account
module "topic_acl_consumer" {
  source             = "./modules/topic_acls"
  for_each           = { for sa_key, sa in var.service_accounts_consumer : sa_key => sa.acl_prefixes }
  resource_names     = each.value
  principal_id       = confluent_service_account.consumer[each.key].id
  operation          = "READ"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

module "group_acl_consumer" {
  source             = "./modules/topic_acls"
  for_each           = { for sa_key, sa in var.service_accounts_consumer : sa_key => sa.acl_prefixes }
  resource_names     = each.value
  principal_id       = confluent_service_account.consumer[each.key].id
  resource_type      = "GROUP"
  operation          = "READ"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}



## DONE FOREACH
