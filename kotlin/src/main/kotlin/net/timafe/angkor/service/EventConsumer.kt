package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.data.PojoCloudEventData
import io.cloudevents.jackson.JsonCloudEventData
import io.cloudevents.jackson.PojoCloudEventDataMapper
import io.cloudevents.kafka.CloudEventDeserializer
import jakarta.annotation.PostConstruct
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import net.timafe.angkor.security.SecurityUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
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

    /**
     * Initialize Kafka Consumer properties programmatically to have more control
     */
    @PostConstruct
    fun init() {
        log.info("[Kafka] Event Consumer initialized with kafkaSupport=${eventService.kafkaEnabled(EventService.KafkaCategory.CONSUMER)} producerBootstrapServers=${kafkaProperties.bootstrapServers}")
        // https://kafka.apache.org/documentation.html#consumerconfigs
        this.consumerProps = Properties()
        this.consumerProps.putAll(eventService.kafkaBaseProperties(EventService.KafkaCategory.CONSUMER))
        // Consumer props which will raise a warning if used for producer
        this.consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = appProps.kafka.topicOverride.ifEmpty { "app" } + ".consumer"
        this.consumerProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        this.consumerProps[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] = "1000"
        this.consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest" // latest, earliest
        this.consumerProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "30000"
        this.consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        // Wrap CloudEvent Deserializer from cloudevents sdk in Error handler
        // since not all events may conform to the expected format (risk of poison message)
        // https://docs.spring.io/spring-kafka/reference/kafka/serdes.html#error-handling-deserializer
        // this.consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = CloudEventDeserializer::class.java.name
        this.consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java.name
        this.consumerProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] =  CloudEventDeserializer::class.java.name
        // this.consumerProps["spring.deserializer.value.delegate.class"] = CloudEventDeserializer::class.java.name
    }


    // CAUTION: each call of consumeMessages requires an active DB Connection from the Pool
    // Value increased to 300000 (5min) to increase the time that hikari cp can be scaled to 0
    // durations are in milliseconds. also supports ${my.delay.property} (escape with \ or kotlin compiler complains)
    // 600000 = 10 Minutes make sure @EnableScheduling is active in AsyncConfig 600000 = 10 min, 3600000 = 1h

    // TIP: #{__listener.publicTopic} Starting with version 2.1.2, the SpEL expressions support a special token: __listener. It is a pseudo bean name
    //  that represents the current bean instance within which this annotation exists.
    // https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html#annotation-properties
    // https://stackoverflow.com/a/27817678/4292075
    @Scheduled(
        fixedRateString = $$"${app.kafka.fixed-rate-seconds}",
        initialDelay = 10,
        timeUnit = TimeUnit.SECONDS,
    )
    // @Scheduled(fixedRateString = "300000", initialDelay = 20000)
    fun consumeMessages() {
        if (!eventService.kafkaEnabled(EventService.KafkaCategory.CONSUMER)) {
            log.warn("$logPrefix Kafka Consumption is not enabled, skipping consumeMessages()")
            return
        }
        // https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm
        val consumer: KafkaConsumer<String, CloudEvent> = KafkaConsumer<String, CloudEvent>(this.consumerProps)
        // val topics = listOf("imagine", "audit", "system", "app").map { "${appProps.kafka.topicPrefix}$it" }
        val topics = listOf( appProps.kafka.topicOverride.ifEmpty{"app.events"}) //

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
            val cloudEvent = record.value()
            // In case of serialization errors, the value is null .. TODO loging
            // https://www.confluent.io/blog/spring-kafka-can-your-kafka-consumers-handle-a-poison-pill/
            if (cloudEvent == null) {
                // https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#listener-error-handlers
                // val errorData = record.headers().headers(KafkaHeaders.RAW_DATA)
                log.warn("$logPrefix Polled record #$received topic=${record.topic()}, partition/offset=${record.partition()}/${record.offset()} has null CloudEvent value, skipping")
                continue
            }
            log.info("$logPrefix Polled valid cloud event #$received topic=${record.topic()}, partition/offset=${record.partition()}/${record.offset()}, key=${record.key()}, value=$cloudEvent")
            val ceDataPayload = cloudEvent.data
            // TODO check https://cloudevents.github.io/sdk-java/json-jackson.html#mapping-cloudeventdata-to-pojos-using-jackson-objectmapper
            if (ceDataPayload is JsonCloudEventData) {
                // extract data payload if it contains our internal
                val ceDataPayload: PojoCloudEventData<Event?>? = mapData(
                    record.value(),
                    PojoCloudEventDataMapper.from(objectMapper, Event::class.java)
                )
                log.info("$logPrefix Mapped CloudEvent data to PojoCloudEventData Value: ${ceDataPayload?.value}")
                val eventEntity = ceDataPayload?.value
                if (eventEntity != null) {
                    try {
                        // Populate cloud event metadata to our event structure until
                        // Entity refactoring is complete
                        eventEntity.id = UUID.fromString(cloudEvent.id)
                        eventEntity.partition = record.partition()
                        eventEntity.offset = record.offset()
                        eventEntity.topic = record.topic()
                        eventEntity.source = cloudEvent.source?.toString() // overwrite internal source with CE source
                        eventService.save(eventEntity)
                        persisted++
                    } catch (e: Exception) {
                        log.warn("$logPrefix Cannot persist Event entity $eventEntity: ${e.message}")
                    }
                } else {
                    log.warn("$logPrefix CloudEvent data could not be mapped to PojoCloudEventData<Event>")
                }

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

/*
try {
val parsedEvent: Event = objectMapper
    .reader()
    .withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .readValue(eventVal, CloudEvent::class.java)

parsedEvent.topic = record.topic().removePrefix(appProps.kafka.topicPrefix)
parsedEvent.partition = record.partition()
parsedEvent.offset = record.offset()
parsedEvent.id = computeMessageId(record.headers())
eventService.save(parsedEvent)
persisted++
} catch (e: Exception) {
log.warn("$logPrefix Cannot parse $eventVal to Event: ${e.message}")
}
 */
