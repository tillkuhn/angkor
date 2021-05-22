package net.timafe.angkor.service

import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.EventRepository
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
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
    private val appProperties: AppProperties
) : EntityService<Event, Event, UUID>(repo) {

    val kafkaProps = Properties()

    override fun entityType(): EntityType {
        return EntityType.EVENT
    }

    @PostConstruct
    fun init() {
        val kafka = appProperties.kafka
        log.info("Event Service initialized with kafkaSupport=${kafkaEnabled()}")
        if (kafkaEnabled()) {
            // https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
            val jaasTemplate =
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
            val jaasCfg = String.format(jaasTemplate, kafka.saslUsername, kafka.saslPassword)
            val serializer: String = StringSerializer::class.java.name
            val deserializer: String = StringDeserializer::class.java.name
            this.kafkaProps["bootstrap.servers"] = kafka.brokers
            this.kafkaProps["key.serializer"] = serializer
            this.kafkaProps["value.serializer"] = serializer
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
            log.info("Kafka configured for brokers=${kafka.brokers} using ${kafka.saslMechanism} ")
        }
    }

    @Async
    fun publish(topicWithoutPrefix: String, message: String) {
        val topic = qualifiedTopic(topicWithoutPrefix)
        if (kafkaEnabled()) {
            log.debug("Publish event '$message' to $topic async=${Thread.currentThread().name}")
            try {
                val producer: Producer<String, String> = KafkaProducer(kafkaProps)
                val d = Date()
                // topic – The topic the record will be appended to
                // key – The key that will be included in the record
                // value – The record contents
                producer.send(ProducerRecord(topic, d.hashCode().toString(), "$message@$d"))
            } catch (v: InterruptedException) {
                log.error("Error publish to $topic: ${v.message}",v)
            }
        } else {
            log.debug("Kafka is not enabled, only logging event w/o publishing it to $topic")
        }
    }

    private fun kafkaEnabled(): Boolean {
        return appProperties.kafka.enabled
    }

    private fun qualifiedTopic(topic: String): String {
        return appProperties.kafka.topicPrefix + topic
    }

}
