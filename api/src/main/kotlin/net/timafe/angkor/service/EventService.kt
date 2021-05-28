package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.dto.EventMessage
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.repo.EventRepository
import net.timafe.angkor.security.SecurityUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.annotation.PostConstruct


/**
 * Service Implementation for managing [Event]
 */
@Service
@Transactional
class EventService(
    repo: EventRepository,
    private val objectMapper: ObjectMapper,
    private val appProperties: AppProperties,
    private val env: Environment
) : EntityService<Event, Event, UUID>(repo) {

    val kafkaProps = Properties()

    override fun entityType(): EntityType {
        return EntityType.EVENT
    }

    @PostConstruct
    fun init() {
        val kafka = appProperties.kafka
        log.info("Event Service initialized with kafkaSupport=${kafkaEnabled()}")
            // https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
        val jaasTemplate =
            "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
        val jaasCfg = String.format(jaasTemplate, kafka.saslUsername, kafka.saslPassword)
        // val deserializer: String = StringDeserializer::class.java.name
        this.kafkaProps["bootstrap.servers"] = kafka.brokers
        this.kafkaProps["key.serializer"] = StringSerializer::class.java.name
        this.kafkaProps["value.serializer"] =  StringSerializer::class.java.name
        this.kafkaProps["security.protocol"] = "SASL_SSL"
        this.kafkaProps["sasl.mechanism"] = kafka.saslMechanism
        this.kafkaProps["sasl.jaas.config"] = jaasCfg
        // Consumer props which will raise a warning if used for producer
        // this.kafkaProps["group.id"] = "${kafka.topicPrefix}consumer"
        // this.kafkaProps["enable.auto.commit"] = "true"
        // this.kafkaProps["auto.commit.interval.ms"] = "1000"
        // this.kafkaProps["auto.offset.reset"] = "earliest"
        // this.kafkaProps["session.timeout.ms"] = "30000"
        // this.kafkaProps["key.deserializer"] = deserializer
        // this.kafkaProps["value.deserializer"] = deserializer
        log.info("Kafka configured for brokers=${kafka.brokers} using ${kafka.saslMechanism} enabled=${kafka.enabled}")
    }

    @Async
    fun publish(eventTopic: EventTopic, message: EventMessage) {
        if (message.source == null) {
            message.source = env.getProperty("spring.application.name")
        }
        val topic = eventTopic.withPrefix(appProperties.kafka.topicPrefix)
        if (kafkaEnabled()) {
            log.debug("[Kafka] Publish event '$message' to $topic async=${Thread.currentThread().name}")
            try {
                val event = objectMapper.writeValueAsString(message)
                val producer: Producer<String?, String> = KafkaProducer(kafkaProps)
                // topic – The topic the record will be appended to
                // key – The key that will be included in the record
                // value – The record contents
                producer.send(ProducerRecord(topic, eventKey(message), event))
            } catch (v: InterruptedException) {
                log.error("Error publish to $topic: ${v.message}",v)
            }
        } else {
            // TODO use MockProducer
            log.debug("Kafka is not enabled, only logging event w/o publishing it to $topic")
        }
    }

    private fun kafkaEnabled(): Boolean {
        val appEnabled = appProperties.kafka.enabled
        val notTest = env.acceptsProfiles(Profiles.of("!" + Constants.PROFILE_TEST))
        return appEnabled && notTest
    }

    /**
     * If entityId is present, use quick and short Alder32 Checksum to indicate a hash key
     * to ensure all events related to a particular entity will be located on the same partition
     */
    private fun eventKey(event: EventMessage): String? {
        return if (event.entityId == null) {
            null
        } else {
            SecurityUtils.getAdler32Checksum(event.entityId as String).toString()
        }

    }

}
