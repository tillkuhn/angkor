package net.timafe.angkor.domain.enums

import net.timafe.angkor.config.annotations.ManagedEntity
import java.util.*

enum class EntityType(val path: String) {
    AREA("areas"),
    DISH("dishes"),
    EVENT("events"),
    FEED("feeds"), // prefix (still) /links
    LINK("links"),
    NOTE("notes"),
    PLACE("places"),
    TOUR("tours"), // prefix (still) /links
    USER("users"),
    Photo("photos"), // Deliberately mixed case
    POST("posts"),
    VIDEO("videos"); // prefix (still) /links

    /**
     *     simply capitalizes the name
     * ... but the build-in method was deprecated in kotlin, and this is what they came up with :-)
     */
    fun titlecase() = name
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    companion object {
        /**
         * Static utility method that retrieves the EntityType from the @ManagedEntity annotation
         */
        fun fromEntityAnnotation(entity: Any): EntityType {
            val annotations = entity::class.annotations
            val ano =
                annotations.firstOrNull { it is ManagedEntity } as? ManagedEntity // https://stackoverflow.com/a/39806461/4292075
            return ano?.entityType
                ?: throw IllegalArgumentException("${entity::class} does not support ${ManagedEntity::class} Annotation")
        }

        fun <T> fromEntityClass(entityClass: Class<T>): EntityType {
            for (en in values()) {
               if (entityClass.simpleName.lowercase().startsWith(en.name.lowercase())) {
                    return en
                }
            }
            throw IllegalArgumentException("cannot derive any entityType from $entityClass")
        }
    }

}
