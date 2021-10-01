package net.timafe.angkor.domain.enums

import net.timafe.angkor.config.annotations.ManagedEntity
import java.util.*

enum class EntityType(val path: String) {
    DISH("dishes"),
    PLACE("places"),
    NOTE("notes"),
    AREA("areas"),
    USER("users"),
    LINK("links"),
    VIDEO("videos"), // prefix (still) /links
    FEED("feeds"), // prefix (still) /links
    TOUR("tours"), // prefix (still) /links
    EVENT("events");

    // simply capitalize ... but the method was deprecated in kotlin, and this is what they came up with :-)
    fun friendlyName() = name.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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
