package net.timafe.angkor.domain.enums

enum class EventType( val actionPrefix: String) {
    CREATE( "create"),
    UPDATE( "update"),
    DELETE( "delete"),
    STARTUP( "startup"),
    // DISH_SERVED("served", "update"),
    // PLACE_VISITED("visited", "update");
}
