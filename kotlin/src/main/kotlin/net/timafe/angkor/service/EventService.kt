package net.timafe.angkor.service

import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.message.Encoding
import io.cloudevents.jackson.JsonFormat
import io.cloudevents.kafka.CloudEventSerializer
import jakarta.annotation.PostConstruct
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.repo.EventRepository
import net.timafe.angkor.security.SecurityUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Service Implementation for managing [Event]
 * @Transactional is intentionally defined on method and not on class level
 *
 */
@Service
class EventService(
    private val repo: EventRepository,
    private val userService: UserService,
    private val objectMapper: JsonMapper,
    private val appProps: AppProperties,
    private val env: Environment,
    private val kafkaProperties: KafkaProperties,
) : AbstractEntityService<Event, Event, UUID>(repo) {

    // Kafka producer properties including base props that will be populated by init() method
    lateinit var producerProps: Properties

    // Categories to distinguish Kafka Producer/Consumer specific settings in methods
    enum class KafkaCategory {
        PRODUCER, CONSUMER
    }

    override fun entityType(): EntityType {
        return EntityType.Event
    }

    /**
     * Initialize Kafka Producer properties programmatically to have more control
     * Example Code: https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
     */
    @PostConstruct
    fun init() {
        log.info("[Kafka] Event Service initialized with kafkaSupport=${kafkaEnabled(KafkaCategory.PRODUCER)} producerBootstrapServers=${kafkaProperties.bootstrapServers}")
        // Consider moving comply JAAS config templating into init() and just use credentials in application properties
        // val jaasTmpl = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";"
        // val jaasCfg = String.format(jaasTmpl, appProps.kafka.username, appProps.kafka.password)

        // Start with basic and default producer properties from spring application config
        this.producerProps = Properties() // https://kafka.apache.org/documentation.html#producerconfigs
        this.producerProps.putAll(kafkaBaseProperties(KafkaCategory.PRODUCER))
        this.producerProps[ProducerConfig.CLIENT_ID_CONFIG] =
            env.getProperty("spring.application.name") ?: this.javaClass.simpleName

        // Configure the CloudEventSerializer to emit events as JSON structured events
        // Example Code: https://github.com/cloudevents/sdk-java/blob/main/examples/kafka/src/main/java/io/cloudevents/examples/kafka/SampleProducer.java
        this.producerProps["key.serializer"] = StringSerializer::class.java.name
        this.producerProps["value.serializer"] = CloudEventSerializer::class.java.name
        // Acks default 1, i.e. leader will write the record to its local log, but not await full acknowledgement from all followers
        this.producerProps["acks"] = "1"
        // The structured encoding approach keeps event metadata and data together in the payload of the message or request.
        // The binary encoding uses Kafka Headers, and keeps event metadata in the message headers, while the event data is in the payload.
        this.producerProps[CloudEventSerializer.ENCODING_CONFIG] = Encoding.STRUCTURED
        this.producerProps[CloudEventSerializer.EVENT_FORMAT_CONFIG] = JsonFormat.CONTENT_TYPE

        // Avoid “stop-the-world” consumer group re-balances by using cooperative re-balancing
        // https://www.confluent.de/blog/5-things-every-kafka-developer-should-know/#tip-3-cooperative-rebalancing
        // this.consumerProps["partition.assignment.strategy"] = CooperativeStickyAssignor::class.java.name
        log.info("[Kafka] Init finished, brokers=${appProps.kafka.brokers}  enabled=${appProps.kafka.producerEnabled}")
    }

    // Wrap a normal Event into a CloudEvent
    fun toCloudEvent(event: Event): CloudEvent {
        val eventStr = objectMapper
            .writer()
            .withoutFeatures(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(event)

        return CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(
                URI.create(
                    String.format(
                        // Example: dev.timafe.net/angkor-api/application/startup
                        "%s.timafe.net/%s/%s/%s",
                        if (env.activeProfiles.contains(Constants.PROFILE_PROD)) Constants.PROFILE_PROD else Constants.PROFILE_DEV,
                        env.getProperty("spring.application.name"),
                        event.source?.lowercase()?: "unknown",
                        event.action
                    )
                )
            )
            .withType(event.javaClass.name)
            .withDataContentType("application/json")
            //   .withDataSchema(URI.create("event@${Event.VERSION}"))
            .withSubject(event.message)
            .withTime(OffsetDateTime.now(ZoneOffset.UTC))
            .withData(
                "application/json", eventStr.toByteArray(StandardCharsets.UTF_8)
            )
            .build()
    }

    /**
     * Publish a new Event to a Kafka Topic
     */
    @Async
    fun publish(eventTopic: EventTopic, event: Event) {
        val logPrefix = "[KafkaProducer]"
        val topicStr = appProps.kafka.topicOverride.ifEmpty {
            eventTopic.withPrefix(appProps.kafka.topicPrefix) // still needed?
        }
        if (kafkaEnabled(KafkaCategory.PRODUCER)) {
            log.debug(
                "{} Publish event '{}' to {} (override={}) async={} ",
                logPrefix, event, topicStr, appProps.kafka.topicOverride, Thread.currentThread().name
            )
            val ce = toCloudEvent(event)
            try {
                //val producer: Producer<String?, String> = KafkaProducer(producerProps)
                val producer: Producer<String?, CloudEvent> = KafkaProducer(producerProps)
                val producerRecord = ProducerRecord(topicStr, recommendKey(event), ce)
                // Confluent Tip #2: Use the power of record headers
                // https://www.confluent.de/blog/5-things-every-kafka-developer-should-know/#tip-5-record-headers
                producerRecord.headers().add("client_id", ce.id.toByteArray())

                // we will do the more complex handling later
                producer.send(producerRecord)
                log.info("$logPrefix Message successfully published to $topicStr with id=${ce.id}")
            } catch (v: InterruptedException) {
                log.error("$logPrefix Error publish to $topicStr: ${v.message}", v)
            }
        } else {
            // TODO use MockProducer
            log.debug("$logPrefix Kafka is not enabled, only logging event w/o publishing it to $topicStr")
        }
    }

    // Other services with @Scheduled annotated method (e.g. EventConsumer) run *without* Auth Context,
    // so we use a special ServiceAccountToken here to support transactions
    @Transactional(readOnly = true)
    fun authenticate() {
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
    }

    fun kafkaBaseProperties(enabledCategory: KafkaCategory): Properties {
        val baseProps = Properties()
        baseProps["bootstrap.servers"] = kafkaProperties.bootstrapServers
        baseProps["security.protocol"] = kafkaProperties.security.protocol
        baseProps["sasl.mechanism"] = kafkaProperties.properties["sasl.mechanism"]
            ?: throw IllegalArgumentException("sasl.mechanism not configured")
        val catProps = when (enabledCategory) {
            KafkaCategory.CONSUMER -> kafkaProperties.consumer.properties
            KafkaCategory.PRODUCER -> kafkaProperties.producer.properties
        }
        if (kafkaEnabled(enabledCategory)) {
            baseProps["sasl.jaas.config"] = catProps["sasl.jaas.config"]
                ?: throw IllegalArgumentException("sasl.jaas.config not configured for $enabledCategory")
        }
        return baseProps
    }

    fun kafkaEnabled(enabledCategory: KafkaCategory): Boolean {
        val appEnabled = when (enabledCategory) {
            KafkaCategory.CONSUMER -> appProps.kafka.consumerEnabled
            KafkaCategory.PRODUCER -> appProps.kafka.producerEnabled
        }
        val notTest = env.acceptsProfiles(Profiles.of("!" + Constants.PROFILE_TEST))
        return appEnabled && notTest
    }

    /**
     * If entityId is present, use quick and short Alder32 Checksum to indicate a hash key
     * to ensure all events related to a particular entity will be located on the same partition
     */
    private fun recommendKey(event: Event): String? {
        return if (event.entityId == null) {
            OffsetDateTime.now(ZoneOffset.UTC).toString()
        } else {
            SecurityUtils.getAdler32Checksum(event.entityId.toString()).toString()
        }

    }

    /**
     * Get the latest events any topic
     * (and finally ... some JPA)
     */
    @Transactional(readOnly = true)
    fun findLatest(): List<Event> {
        val items = repo.findFirst50ByOrderByTimeDesc()
        this.log.info("${logPrefix()} findLatest: ${items.size} results")
        return items
    }

    /**
     * Get the latest events filtered by topic
     */
    @Transactional(readOnly = true)
    fun findLatestByTopic(topic: String): List<Event> {
        val items = repo.findFirst50ByTopicOrderByTimeDesc(topic)
        this.log.info("${logPrefix()} findLatestByTopic: ${items.size} results for topic=$topic")
        return items
    }


}
