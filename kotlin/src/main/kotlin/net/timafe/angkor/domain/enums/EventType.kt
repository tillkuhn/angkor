package net.timafe.angkor.domain.enums

import java.util.*

/**
 * Generic Event Types
 *
 * used to construct Event identifiers used for Events published to Kafka
 */
enum class EventType( val actionPrefix: String) {
    CREATE( "create"),
    UPDATE( "update"),
    DELETE( "delete"),
    STARTUP( "startup");
    // DISH_SERVED("served", "update"),
    // PLACE_VISITED("visited", "update");

    fun titlecase() = actionPrefix
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
