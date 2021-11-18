package net.timafe.angkor.domain.enums

enum class EventTopic(val topic: String) {

    AUDIT("audit"),
    APP("app"),
    SYSTEM("system");

    fun addPrefix(prefix: String) = prefix + topic
}
