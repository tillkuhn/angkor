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


# Attach registry schema for app.events
# https://registry.terraform.io/providers/confluentinc/confluent/latest/docs/resources/confluent_schema
resource "confluent_schema" "app_events" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.main.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.main.rest_endpoint
  subject_name  = "${var.topic_acl_app_prefix}events-value" # - value suffix is essential to match the topic
  format        = "JSON"                                    # or "AVRO"
  schema        = file("${path.module}/schemas/cloudevents.json")
  credentials {
    key    = confluent_api_key.env_manager_schema_registry_api_key.id
    secret = confluent_api_key.env_manager_schema_registry_api_key.secret
  }
}

# Let's switch gears and add some consumer / producer ACLs for fine grained permissions, Inspired by

# App Producer Service Account & API Key
resource "confluent_service_account" "app_producer" {
  display_name = "app-producer"
  description  = "Service account to produce to 'public' topics of 'inventory' Kafka cluster"
}

# App Producer Service Account & API Key
resource "confluent_service_account" "dev_producer" {
  display_name = "dev-producer"
  description  = "Service account to produce to 'dev' topics of 'inventory' Kafka cluster"
}

# API key for application(s) to produce events
resource "confluent_api_key" "app_producer_kafka_api_key" {
  display_name = "app-producer-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-producer' service account"
  owner {
    id          = confluent_service_account.app_producer.id
    api_version = confluent_service_account.app_producer.api_version
    kind        = confluent_service_account.app_producer.kind
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

# API key for development to produce events (any dev prefixed topic)
resource "confluent_api_key" "dev_producer_kafka_api_key" {
  display_name = "dev-producer-kafka-api-key"
  description  = "Kafka API Key for development that is owned by 'app-producer' service account"
  owner {
    id          = confluent_service_account.dev_producer.id
    api_version = confluent_service_account.dev_producer.api_version
    kind        = confluent_service_account.dev_producer.kind
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

# ACL WRITE 'app.* '
module "topic_acl_app_producer_app" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_app_prefix
  principal_id       = confluent_service_account.app_producer.id
  operation          = "WRITE"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# ACL WRITE 'dev.* '
module "topic_acl_dev_producer" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_dev_prefix
  principal_id       = confluent_service_account.dev_producer.id
  operation          = "WRITE"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# ACL WRITE 'system.* '
module "topic_acl_app_producer_system" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_system_prefix
  principal_id       = confluent_service_account.app_producer.id
  operation          = "WRITE"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# ACL WRITE 'public.* '
module "topic_acl_app_producer_public" {
  source             = "./modules/topic_acl"
  operation          = "WRITE"
  resource_name      = var.topic_acl_public_prefix
  principal_id       = confluent_service_account.app_producer.id
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# Additional Producer Account + key for GitHub ...
resource "confluent_service_account" "ci_producer" {
  display_name = "ci-producer"
  description  = "Service account to produce to 'ci' topics, e.g. for GitHub Actions"
}

resource "confluent_api_key" "ci_producer_kafka_api_key" {
  display_name = "ci-producer-kafka-api-key"
  description  = "Kafka API Key that is owned by 'ci-producer' service account"
  owner {
    id          = confluent_service_account.ci_producer.id
    api_version = confluent_service_account.ci_producer.api_version
    kind        = confluent_service_account.ci_producer.kind
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

# ACL 'ci.* ' for CI Producer
module "topic_acl_ci_producer" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_ci_prefix
  principal_id       = confluent_service_account.ci_producer.id
  operation          = "WRITE"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

############
# CONSUMER #
############
resource "confluent_service_account" "app_consumer" {
  display_name = "app-consumer"
  description  = "Service account to consume from topics of 'inventory' Kafka cluster"
}

resource "confluent_api_key" "app_consumer_kafka_api_key" {
  display_name = "app-consumer-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-consumer' service account"
  owner {
    id          = confluent_service_account.app_consumer.id
    api_version = confluent_service_account.app_consumer.api_version
    kind        = confluent_service_account.app_consumer.kind
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

# Note that in order to consume from a topic, the principal of the consumer ('app-consumer' service account)
# needs to be authorized to perform 'READ' operation on both TOPIC and (Consumer) GROUP resources:
# confluent_kafka_acl.app-consumer-read-on-topic, confluent_kafka_acl.app-consumer-read-on-group.
# https://docs.confluent.io/platform/current/kafka/authorization.html#using-acls
# ACL READ 'app.* '
module "topic_acl_app_consumer_app" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_app_prefix
  principal_id       = confluent_service_account.app_consumer.id
  operation          = "READ"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# ACL READ 'ci.* '
module "topic_acl_app_consumer_ci" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_ci_prefix
  principal_id       = confluent_service_account.app_consumer.id
  operation          = "READ"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# ACL READ 'system.* '
module "topic_acl_app_consumer_system" {
  source             = "./modules/topic_acl"
  resource_name      = var.topic_acl_system_prefix
  principal_id       = confluent_service_account.app_consumer.id
  operation          = "READ"
  cluster_id         = confluent_kafka_cluster.default.id
  cluster_endpoint   = confluent_kafka_cluster.default.rest_endpoint
  cluster_api_key    = confluent_api_key.cluster.id
  cluster_api_secret = confluent_api_key.cluster.secret
}

# Consumer Group ID must also correspond with the following Item
resource "confluent_kafka_acl" "app_consumer_read_group" {
  kafka_cluster {
    id = confluent_kafka_cluster.default.id
  }
  rest_endpoint = confluent_kafka_cluster.default.rest_endpoint
  resource_type = "GROUP"
  resource_name = var.topic_acl_group_prefix
  pattern_type  = "PREFIXED"
  operation     = "READ"
  principal     = "User:${confluent_service_account.app_consumer.id}"
  host          = "*"
  permission    = "ALLOW"
  credentials {
    key    = confluent_api_key.cluster.id
    secret = confluent_api_key.cluster.secret
  }
}


#resource "hcp_vault_secrets_secret" "confluent_cluster_producer_key" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_producer_key"
#  secret_value = confluent_api_key.app_producer_kafka_api_key.id
#}
#
#resource "hcp_vault_secrets_secret" "confluent_cluster_producer_secret" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_producer_secret"
#  secret_value = confluent_api_key.app_producer_kafka_api_key.secret
#}

# store cloud api key in some store for distribution (e.g. vault, AWS SSM ..)
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

# Store API Key / Secrets in new HCP Vault

# Setup new HashiCorp Cloud Platform App Secrets Store"
# DISABLED ... CONTROL SHOULD BE IN THE PARENT PROJECT
#
#resource "hcp_vault_secrets_app" "main" {
#  app_name    = var.hcp_vault_secrets_app_name
#  description = "HCP Secrets Store for ${var.app_id} App"
#}
#
#resource "hcp_vault_secrets_secret" "confluent_cluster_api_key_key" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_api_key_key"
#  secret_value = confluent_api_key.cluster.id
#}
#
#resource "hcp_vault_secrets_secret" "confluent_cluster_api_key_secret" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_api_key_secret"
#  secret_value = confluent_api_key.cluster.secret
#}
#
## useful for  -H "Authorization:Basic <token>" header in combination with Confluent REST API
#resource "hcp_vault_secrets_secret" "confluent_producer_basic_auth" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_producer_basic_auth"
#  secret_value = base64encode("${confluent_api_key.cluster.id}:${confluent_api_key.cluster.secret}")
#}
#
#
#resource "hcp_vault_secrets_secret" "confluent_cluster_rest_endpoint" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_rest_endpoint"
#  secret_value = confluent_kafka_cluster.default.rest_endpoint
#}
#
#resource "hcp_vault_secrets_secret" "confluent_cluster_id" {
#  app_name     = hcp_vault_secrets_app.main.app_name
#  secret_name  = "confluent_cluster_id"
#  secret_value = confluent_kafka_cluster.default.id
#}

