package net.timafe.angkor.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import net.timafe.angkor.security.SecurityUtils
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Service Implementation for consuming [Event]
 */
@Service
class EventConsumer(
    // private val repo: EventRepository,
    private val objectMapper: ObjectMapper,
    private val appProps: AppProperties,
    private val kafkaProperties: KafkaProperties,
    private val eventService: EventService,

    ) {
    // Populated by init
    lateinit var consumerProps: Properties

    private val log = LoggerFactory.getLogger(javaClass)
    val logPrefix = "[KafkaConsumer]"

    @PostConstruct
    fun init() {
        log.info("[Kafka] Event Consumer initialized with kafkaSupport=${eventService.kafkaEnabled()} producerBootstrapServers=${kafkaProperties.bootstrapServers}")
        // https://kafka.apache.org/documentation.html#consumerconfigs
        this.consumerProps = Properties()
        this.consumerProps.putAll(eventService.kafkaBaseProperties())
        // Consumer props which will raise a warning if used for producer
        this.consumerProps["group.id"] = "${appProps.kafka.topicPrefix}hase"
        this.consumerProps["enable.auto.commit"] = "true"
        this.consumerProps["auto.commit.interval.ms"] = "1000"
        this.consumerProps["auto.offset.reset"] = "earliest"
        this.consumerProps["session.timeout.ms"] = "30000"
        this.consumerProps["key.deserializer"] = StringDeserializer::class.java.name
        this.consumerProps["value.deserializer"] = StringDeserializer::class.java.name

    }

    // CAUTION: each call of consumeMessages requires an active DB Connection from the Pool
    // Value increased to 300000 (5min) to increase the time that hikari cp can be scaled to 0
    // durations are in milliseconds. also supports ${my.delay.property} (escape with \ or kotlin compiler complains)
    // 600000 = 10 Minutes make sure @EnableScheduling is active in AsyncConfig 600000 = 10 min, 3600000 = 1h

    // #{__listener.publicTopic} Starting with version 2.1.2, the SpEL expressions support a special token: __listener. It is a pseudo bean name
    //  that represents the current bean instance within which this annotation exists.
    // https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html#annotation-properties
    // https://stackoverflow.com/a/27817678/4292075
    @Scheduled(
        fixedRateString = "\${app.kafka.fixed-rate-seconds}",
        initialDelay = 20,
        timeUnit = TimeUnit.SECONDS,
    )
    // @Scheduled(fixedRateString = "300000", initialDelay = 20000)
    fun consumeMessages() {
        if (!eventService.kafkaEnabled()) {
            log.warn("$logPrefix Kafka is not enabled, skipping consumeMessages()")
            return
        }
        // https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm
        // https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
        val consumer: KafkaConsumer<String, String> = KafkaConsumer<String, String>(this.consumerProps)
        val topics = listOf("imagine", "audit", "system", "app").map { "${appProps.kafka.topicPrefix}$it" }
        log.debug(" {} Consuming fresh messages from kafka topics {}", logPrefix, topics)
        consumer.subscribe(topics)
        var (received, persisted) = listOf(0, 0)
        val records = consumer.poll(Duration.ofMillis(10L * 1000))
        if (! records.isEmpty) {
            // Lazy invoke authenticate which is marked @Transactional
            // Advantage: We don't need a transaction if there are no events to persist,
            // so we can keep the active connection pool count at zero (nice for neon db)
            eventService.authenticate()
        }
        for (record in records) {
            val eventVal = record.value()
            log.info("$logPrefix Polled record #$received topic=${record.topic()}, partition/offset=${record.partition()}/${record.offset()}, key=${record.key()}, value=$eventVal")
            try {
                val parsedEvent: Event = objectMapper
                    .reader()
                    .withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(eventVal, Event::class.java)

                parsedEvent.topic = record.topic().removePrefix(appProps.kafka.topicPrefix)
                parsedEvent.partition = record.partition()
                parsedEvent.offset = record.offset()
                parsedEvent.id = computeMessageId(record.headers())
                eventService.save(parsedEvent)
                persisted++
            } catch (e: Exception) {
                log.warn("$logPrefix Cannot parse $eventVal to Event: ${e.message}")
            }
            received++
        }
        if (received > 0) {
            log.info("$logPrefix Polled $received records ($persisted persisted), see you again at a fixed rate")
        } else {
            log.trace("$logPrefix No records to poll in this run")
        }
        consumer.close()
    }

    fun computeMessageId(headers: Headers): UUID {
        for (header in headers) {
            if (header.key().toString() == "messageId") {
                val mid = String(header.value())
                val midUUID = SecurityUtils.safeConvertToUUID(mid)
                return if (midUUID == null) {
                    log.warn("$logPrefix Could not convert messageId $mid to UUID, generating new one")
                    UUID.randomUUID()
                } else {
                    log.debug("{} using messageId from header {}", logPrefix, midUUID)
                    midUUID
                }
            }
        }
        val newId = UUID.randomUUID()
        log.warn("$logPrefix Could not find messageId in any header, generated $newId")
        return newId
    }
}
