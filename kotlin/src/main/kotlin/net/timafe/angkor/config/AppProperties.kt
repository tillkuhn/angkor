package net.timafe.angkor.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties specific to this app
 *
 * Properties are configured in the `application.yml` file.
 * See JHipsterProperties for a good example
 * and https://www.jhipster.tech/common-application-properties/#2
 */
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
class AppProperties {

    class Tours {
        var importFolder = ""
        var apiBaseUrl: String = ""
        var apiUserId: String = ""
    }

    class Photos {
        var feedUrl = ""
    }

    class Kafka {
        var enabled = false
        var brokers = ""
        var saslUsername = ""
        var saslPassword = ""
        var topicPrefix = ""
        var saslMechanism = "SCRAM-SHA-256"
        // var clientId = "angkor-api"
    }

    var apiToken: String = ""
    var apiTokenHeader: String = "X-Auth-Token"
    var version: String = "latest"
    var externalBaseUrl: String = ""
    var osmApiBaseUrl: String = ""

    val kafka = Kafka()

    val tours = Tours()
    val photos = Photos()
}
