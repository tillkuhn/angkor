package net.timafe.angkor.domain.enums

enum class EventTopic(val topic: String) {

    AUDIT("audit.events"),
    APP("app.events"),
    SYSTEM("system.events");

    fun withPrefix(prefix: String) = prefix + topic

    companion object {
        /**
         * Returns a list of all topic names, suitable for subscriptions in a KafkaConsumer service
         */
        fun allTopics(): List<String> = entries.map { it.topic }
    }
}
