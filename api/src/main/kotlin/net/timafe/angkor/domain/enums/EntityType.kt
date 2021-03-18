package net.timafe.angkor.domain.enums

import net.timafe.angkor.config.annotations.ManagedEntity

enum class EntityType(val path: String) {
    DISH("dishes"),
    PLACE("places"),
    NOTE("notes"),
    AREA("areas"),
    USER("users"),
    LINK("links"),
    VIDEO("videos"),
    EVENT("events");

    fun friendlyName() = name.toLowerCase().capitalize()

    companion object {
        fun fromEntityAnnotation(entity: Any): EntityType {
            val annotations = entity::class.annotations
            val ano =
                annotations.firstOrNull { it is ManagedEntity } as? ManagedEntity // https://stackoverflow.com/a/39806461/4292075
            return ano?.entityType
                ?: throw IllegalArgumentException("${entity::class} does not support ${ManagedEntity::class} Annotation")
        }
    }
}
