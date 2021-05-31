package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.repo.EventRepository
import net.timafe.angkor.security.SecurityUtils
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import java.time.Duration
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
    private val appProps: AppProperties,
    private val env: Environment
) : AbstractEntityService<Event, Event, UUID>(repo) {

    lateinit var producerProps: Properties
    lateinit var consumerProps: Properties

    override fun entityType(): EntityType {
        return EntityType.EVENT
    }

    @PostConstruct
    fun init() {
        log.info("Event Service initialized with kafkaSupport=${kafkaEnabled()}")
        // https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
        val jaasTemplate =
            "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
        val jaasCfg = String.format(jaasTemplate, appProps.kafka.saslUsername, appProps.kafka.saslPassword)
        val baseProps = Properties()
        baseProps["bootstrap.servers"] = appProps.kafka.brokers
        baseProps["security.protocol"] = "SASL_SSL"
        baseProps["sasl.mechanism"] = appProps.kafka.saslMechanism
        baseProps["sasl.jaas.config"] = jaasCfg

        this.producerProps = Properties()
        this.producerProps.putAll(baseProps)
        this.producerProps["key.serializer"] = StringSerializer::class.java.name
        this.producerProps["value.serializer"] = StringSerializer::class.java.name

        this.consumerProps = Properties()
        this.consumerProps.putAll(baseProps)
        // Consumer props which will raise a warning if used for producer
        this.consumerProps["group.id"] = "${appProps.kafka.topicPrefix}hase"
        this.consumerProps["enable.auto.commit"] = "true"
        this.consumerProps["auto.commit.interval.ms"] = "1000"
        this.consumerProps["auto.offset.reset"] = "earliest" // earliest
        this.consumerProps["session.timeout.ms"] = "30000"
        this.consumerProps["key.deserializer"] = StringDeserializer::class.java.name
        this.consumerProps["value.deserializer"] = StringDeserializer::class.java.name

        log.info("Kafka configured for brokers=${appProps.kafka.brokers} using ${appProps.kafka.saslMechanism} enabled=${appProps.kafka.enabled}")
    }

    @Async
    fun publish(eventTopic: EventTopic, event: Event) {
        val logPrefix = "[KafkaProducer]"
        val topic = eventTopic.addPrefix(appProps.kafka.topicPrefix)

        event.source = event.source ?: env.getProperty("spring.application.name")
        if (kafkaEnabled()) {
            log.debug("$logPrefix Publish event '$event' to $topic async=${Thread.currentThread().name}")
            try {
                val eventStr = objectMapper.writer().withoutFeatures(SerializationFeature.INDENT_OUTPUT).writeValueAsString(event)
                val producer: Producer<String?, String> = KafkaProducer(producerProps)
                // topic – The topic the record will be appended to
                // key – The key that will be included in the record
                // value – The record contents
                producer.send(ProducerRecord(topic, recommendKey(event), eventStr))
            } catch (v: InterruptedException) {
                log.error("$logPrefix Error publish to $topic: ${v.message}", v)
            }
        } else {
            // TODO use MockProducer
            log.debug("$logPrefix Kafka is not enabled, only logging event w/o publishing it to $topic")
        }
    }

    // durations are in milliseconds. also supports ${my.delay.property} (escape with \ or kotlin compiler complains)
    // 600000 = 10 Minutes.. make sure @EnableScheduling is active in AsyncConfig 600000 = 10 min, 3600000 = 1h
    @Scheduled(fixedRateString = "30000", initialDelay = 5000)
    @Transactional
    fun consumeMessages() {
        val logPrefix = "[KafkaConsumerLoop]"
        // https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm
        // https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
        val consumer: KafkaConsumer<String, String> = KafkaConsumer<String, String>(this.consumerProps)
        val topics = listOf("imagine", "audit","system","app").map { "${appProps.kafka.topicPrefix}$it" }
        log.trace(" $logPrefix I'm here to consume ... new Kafka Messages from topics $topics soon!")
        consumer.subscribe(topics)
        var received = 0
        val records = consumer.poll(Duration.ofMillis(10000))
        for (record in records) {
            val eventVal = record.value()
            log.info("$logPrefix new record topic=${record.topic()}, offset=${record.offset()}, partion=${record.partition()}, key=${record.key()}, value=$eventVal")
            try {
                val parsedEvent: Event = objectMapper.readValue(eventVal, Event::class.java)
                parsedEvent.topic = record.topic().removePrefix(appProps.kafka.topicPrefix)
                parsedEvent.partition = record.partition()
                parsedEvent.recordOffset = record.offset()
                super.save(parsedEvent)
            } catch (e: Exception) {
                log.warn("$logPrefix Cannot parse $eventVal to Event: ${e.message}")
            }
            received++
        }
        if (received > 0) {
            log.info("$logPrefix Done polling $received records, see you again at a fixed rate")
        } else {
            log.trace("$logPrefix No records to poll in this run")
        }
        consumer.close()
    }

    private fun kafkaEnabled(): Boolean {
        val appEnabled = appProps.kafka.enabled
        val notTest = env.acceptsProfiles(Profiles.of("!" + Constants.PROFILE_TEST))
        return appEnabled && notTest
    }

    /**
     * If entityId is present, use quick and short Alder32 Checksum to indicate a hash key
     * to ensure all events related to a particular entity will be located on the same partition
     */
    private fun recommendKey(event: Event): String? {
        return if (event.entityId == null) {
            null
        } else {
            SecurityUtils.getAdler32Checksum(event.entityId.toString()).toString()
        }

    }

}
