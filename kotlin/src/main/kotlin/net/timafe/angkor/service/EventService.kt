package net.timafe.angkor.service

import com.fasterxml.jackson.databind.DeserializationFeature
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
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct


/**
 * Service Implementation for managing [Event]
 */
@Service
@Transactional
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
    lateinit var consumerProps: Properties

    override fun entityType(): EntityType {
        return EntityType.Event
    }

    @PostConstruct
    fun init() {
        log.info("[Kafka] Event Service initialized with kafkaSupport=${kafkaEnabled()} producerBootstrapServers=${kafkaProperties.producer.bootstrapServers}")
        // https://github.com/CloudKarafka/java-kafka-example/blob/master/src/main/java/KafkaExample.java
        // val jaasTemplate =
            "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
        // val jaasCfg = String.format(jaasTemplate, appProps.kafka.saslUsername, appProps.kafka.saslPassword)

        val baseProps = Properties()
        baseProps["bootstrap.servers"] = kafkaProperties.bootstrapServers
        baseProps["security.protocol"] = kafkaProperties.security.protocol
        baseProps["sasl.mechanism"] = kafkaProperties.properties["sasl.mechanism"]?:throw IllegalArgumentException("sasl.mechanism not configured")
        baseProps["sasl.jaas.config"] = kafkaProperties.properties["sasl.jaas.config"] // jaasCfg

        // https://kafka.apache.org/documentation.html#producerconfigs
        this.producerProps = Properties()
        this.producerProps.putAll(baseProps)
        this.producerProps["key.serializer"] = StringSerializer::class.java.name
        this.producerProps["value.serializer"] = StringSerializer::class.java.name
        // default 1, leader will write the record to its local log but not await full acknowledgement from all followers
        this.producerProps["acks"] = "1"

        // https://kafka.apache.org/documentation.html#consumerconfigs
        this.consumerProps = Properties()
        this.consumerProps.putAll(baseProps)
        // Consumer props which will raise a warning if used for producer
        this.consumerProps["group.id"] = "${appProps.kafka.topicPrefix}hase"
        this.consumerProps["enable.auto.commit"] = "true"
        this.consumerProps["auto.commit.interval.ms"] = "1000"
        this.consumerProps["auto.offset.reset"] = "earliest"
        this.consumerProps["session.timeout.ms"] = "30000"
        this.consumerProps["key.deserializer"] = StringDeserializer::class.java.name
        this.consumerProps["value.deserializer"] = StringDeserializer::class.java.name
        // https://www.confluent.de/blog/5-things-every-kafka-developer-should-know/#tip-3-cooperative-rebalancing
        // Avoid “stop-the-world” consumer group re-balances by using cooperative re-balancing
        // this.consumerProps["partition.assignment.strategy"] = CooperativeStickyAssignor::class.java.name
        log.info("[Kafka] Init finished, brokers=${appProps.kafka.brokers} using ${appProps.kafka.saslMechanism} enabled=${appProps.kafka.enabled}")
    }

    /**
     * Publish a new Event to a Kafka Topic
     */
    @Async
    fun publish(eventTopic: EventTopic, event: Event) {
        val logPrefix = "[KafkaProducer]"
        val clientId = env.getProperty("spring.application.name")?:this.javaClass.simpleName
        val topic = eventTopic.withPrefix(appProps.kafka.topicPrefix)

        event.source = event.source ?: env.getProperty("spring.application.name")
        if (kafkaEnabled()) {
            log.debug("$logPrefix Publish event '$event' to $topic async=${Thread.currentThread().name}")
            try {
                val eventStr = objectMapper
                    .writer()
                    .withoutFeatures(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(event)
                val producer: Producer<String?, String> = KafkaProducer(producerProps)
                // topic – The topic the record will be appended to
                // key – The key that will be included in the record
                // value – The record contents
                val producerRecord = ProducerRecord(topic, recommendKey(event), eventStr)
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
                log.error("$logPrefix Error publish to $topic: ${v.message}", v)
            }
        } else {
            // TODO use MockProducer
            log.debug("$logPrefix Kafka is not enabled, only logging event w/o publishing it to $topic")
        }
    }

    // durations are in milliseconds. also supports ${my.delay.property} (escape with \ or kotlin compiler complains)
    // 600000 = 10 Minutes make sure @EnableScheduling is active in AsyncConfig 600000 = 10 min, 3600000 = 1h
    @Scheduled(fixedRateString = "120000", initialDelay = 10000)
    @Transactional
    fun consumeMessages() {
        // @Scheduled runs without Auth Context, so we use a special ServiceAccountToken here
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)

        val logPrefix = "[KafkaConsumerLoop]"
        // https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm
        // https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
        val consumer: KafkaConsumer<String, String> = KafkaConsumer<String, String>(this.consumerProps)
        val topics = listOf("imagine", "audit", "system", "app").map { "${appProps.kafka.topicPrefix}$it" }
        log.trace(" $logPrefix I'm here to consume ... new Kafka Messages from topics $topics soon!")
        consumer.subscribe(topics)
        var (received, persisted) = listOf(0, 0)
        val records = consumer.poll(Duration.ofMillis(10L * 1000))
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
                super.save(parsedEvent)
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

    fun computeMessageId(headers: Headers): UUID {
        for (header in headers) {
            if (header.key().toString() == "messageId") {
                val mid = String(header.value())
                val midUUID = SecurityUtils.safeConvertToUUID(mid)
                return if (midUUID == null) {
                    log.warn("${logPrefix()} Could not convert messageId $mid to UUID, generating new one")
                    UUID.randomUUID()
                } else {
                    log.debug("${logPrefix()} using messageId from header $midUUID")
                    midUUID
                }
            }
        }
        log.warn("${logPrefix()} Could not find messageId in any header, generating new onw")
        return UUID.randomUUID()
    }

}
