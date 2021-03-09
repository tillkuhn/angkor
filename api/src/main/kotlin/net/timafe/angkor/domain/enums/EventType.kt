package net.timafe.angkor.domain.enums

enum class EventType(val verb: String) {
    CREATED("created"),
    UPDATED("updated"),
    DELETED("removed"),
    DISH_SERVED("served"),
    PLACE_VISITED("visited");
}
