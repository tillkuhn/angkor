package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
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
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.toString


/**
 * Service Implementation for managing [Event]
 */
@Service
// @Transactional let's better define on method level
class EventService(
    private val repo: EventRepository,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val appProps: AppProperties,
    private val env: Environment,
    private val kafkaProperties: KafkaProperties,
    ) : AbstractEntityService<Event, Event, UUID>(repo) {

    // Kafka properties that will be populated by init() method
    lateinit var producerProps: Properties
    enum class KafkaCategory {
        PRODUCER, CONSUMER
    }
    override fun entityType(): EntityType {
        return EntityType.Event
    }

    @PostConstruct
    fun init() {
        log.info("[Kafka] Event Service initialized with kafkaSupport=${kafkaEnabled(KafkaCategory.PRODUCER)} producerBootstrapServers=${kafkaProperties.bootstrapServers}")
        // https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
        // val jaasTemplate =
        //    "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
        // val jaasCfg = String.format(jaasTemplate, appProps.kafka.saslUsername, appProps.kafka.saslPassword)

        // https://kafka.apache.org/documentation.html#producerconfigs
        this.producerProps = Properties()
        this.producerProps.putAll(kafkaBaseProperties(KafkaCategory.PRODUCER))
        // this.producerProps["key.serializer"] = StringSerializer::class.java.name
        // this.producerProps["value.serializer"] = StringSerializer::class.java.name
        this.producerProps["key.serializer"] = StringSerializer::class.java.name
        this.producerProps["value.serializer"] = CloudEventSerializer::class.java.name
        // default 1, leader will write the record to its local log but not await full acknowledgement from all followers
        this.producerProps["acks"] = "1"

        // https://www.confluent.de/blog/5-things-every-kafka-developer-should-know/#tip-3-cooperative-rebalancing
        // Avoid “stop-the-world” consumer group re-balances by using cooperative re-balancing
        // this.consumerProps["partition.assignment.strategy"] = CooperativeStickyAssignor::class.java.name
        log.info("[Kafka] Init finished, brokers=${appProps.kafka.brokers}  enabled=${appProps.kafka.producerEnabled}")
    }

    // Wrap a normal Event into a CloudEvent
    //     private val kafkaTemplate: KafkaTemplate<String?, CloudEvent?>
    fun wrapEvent (event: Event): CloudEvent
    {
        val eventStr = objectMapper
            .writer()
            .withoutFeatures(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(event)
        return CloudEventBuilder.v1()
            .withId("1")
            .withSource(URI.create("/captain/horst"))
            .withType(this.javaClass.name)
            .withDataContentType("application/json")
            .withDataSchema(URI.create("hase/event-test"))
            .withSubject("First CloudEvent blog")
            .withTime(OffsetDateTime.now(ZoneOffset.UTC))
            .withData("application/json",eventStr.toByteArray(StandardCharsets.UTF_8)
            )
            .build()
    }

    /**
     * Publish a new Event to a Kafka Topic
     */
    @Async
    fun publish(eventTopic: EventTopic, event: Event) {
        val logPrefix = "[KafkaProducer]"
        val clientId = env.getProperty("spring.application.name")?:this.javaClass.simpleName
        val topicStr = appProps.kafka.topicOverride.ifEmpty {
            eventTopic.withPrefix(appProps.kafka.topicPrefix)
        }
        // by default source applies to the entire app (e.g. angkor-api)
        event.source = event.source ?: env.getProperty("spring.application.name")
        if (kafkaEnabled(KafkaCategory.PRODUCER)) {
            log.debug("{} Publish event '{}' to {} (override={}) async={} ",
                logPrefix, event, topicStr, appProps.kafka.topicOverride, Thread.currentThread().name)
            try {
                //val producer: Producer<String?, String> = KafkaProducer(producerProps)
                val producer: Producer<String?, CloudEvent> = KafkaProducer(producerProps)
                // topic – The topic the record will be appended to
                // key – The key that will be included in the record
                // value – The record contents
                val ce = wrapEvent(event)
                val producerRecord = ProducerRecord(topicStr, recommendKey(event), ce)
                val schema = "event@${Event.VERSION}".toByteArray(/* UTF8 is default */)
                val messageId = UUID.randomUUID().toString()
                // https://www.confluent.de/blog/5-things-every-kafka-developer-should-know/#tip-5-record-headers
                producerRecord.headers().add("messageId", messageId.toByteArray())
                producerRecord.headers().add("schema", schema)
                producerRecord.headers().add("clientId", clientId.toByteArray())

                // we will do the more complex handling later
                producer.send(producerRecord)
                log.info("$logPrefix Message published with id=${messageId}")
            } catch (v: InterruptedException) {
                log.error("$logPrefix Error publish to $topicStr: ${v.message}", v)
            }
        } else {
            // TODO use MockProducer
            log.debug("$logPrefix Kafka is not enabled, only logging event w/o publishing it to $topicStr")
        }
    }

    // Other serviced annotated with @Scheduled runs without Auth Context,
    // so we use a special ServiceAccountToken here, but we need a transaction
    @Transactional(readOnly = true)
    fun authenticate() {
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
    }

    fun kafkaBaseProperties(enabledCategory: KafkaCategory):Properties  {
        val baseProps = Properties()
        baseProps["bootstrap.servers"] = kafkaProperties.bootstrapServers
        baseProps["security.protocol"] = kafkaProperties.security.protocol
        baseProps["sasl.mechanism"] = kafkaProperties.properties["sasl.mechanism"]?:throw IllegalArgumentException("sasl.mechanism not configured")
        val catProps = when (enabledCategory) {
            KafkaCategory.CONSUMER -> kafkaProperties.consumer.properties
            KafkaCategory.PRODUCER -> kafkaProperties.producer.properties
        }
        baseProps["sasl.jaas.config"] = catProps["sasl.jaas.config"]?:throw IllegalArgumentException("sasl.jaas.config not configured for $enabledCategory")
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

    // And finally ... some JPA ...
    /**
     * Get the latest events any topic
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

//    fun send(event: CloudEvent) {
//        val logPrefix = "[KafkaProducer]"
//        //val clientId = env.getProperty("spring.application.name")?:this.javaClass.simpleName
//        val topicStr = appProps.kafka.topicPrefix + "events" // prefix in local app props is "dev."
//        log.info("$logPrefix Sending event to $topicStr: $event")
//        val serialized: ByteArray? = EventFormatProvider
//            .getInstance()
//            .resolveFormat(ContentType.JSON)!!.serialize(event)
//        log.info("$logPrefix Event serialized: $serialized")
//        kafkaTemplate.send(ProducerRecord(topicStr, event.id, event))
//    }

}
