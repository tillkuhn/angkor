package net.timafe.angkor.domain.enums

enum class EventTopic(val topic: String) {

    AUDIT("audit.events"),
    APP("app.events"),
    SYSTEM("system.events");

    fun withPrefix(prefix: String) = prefix + topic

    companion object {
        fun allTopics(): List<String> = entries.map { it.topic }
    }
}
