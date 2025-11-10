package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneOffset


/**
 * Service Implementation for managing [Event] using CNCF CloudEvents
 */
@Service
class CloudEventService(
    private val objectMapper: ObjectMapper,
    private val appProps: AppProperties,
    // private val env: Environment,
    // private val kafkaProperties: KafkaProperties,
    private val kafkaTemplate: KafkaTemplate<String?, CloudEvent?>
) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun wrapEvent (msg: Event): CloudEvent
    {
        return CloudEventBuilder.v1()
            .withId("1")
            .withSource(URI.create("/captain/horst"))
            .withType(this.javaClass.toString())
            .withDataContentType("application/json")
            .withDataSchema(URI.create("hase/event-test"))
            .withSubject("First CloudEvent blog")
            .withTime(OffsetDateTime.now(ZoneOffset.UTC))
            .withData(objectMapper.writeValueAsString(msg).toByteArray(StandardCharsets.UTF_8)
            )
            .build()
    }
    fun send(event: CloudEvent) {
        val logPrefix = "[KafkaProducer]"
        //val clientId = env.getProperty("spring.application.name")?:this.javaClass.simpleName
        val topicStr = appProps.kafka.topicPrefix + "events" // prefix in local app props is "dev."
        log.info("$logPrefix Sending event to $topicStr: $event")
        kafkaTemplate.send(ProducerRecord(topicStr, event.id, event))
    }

}
