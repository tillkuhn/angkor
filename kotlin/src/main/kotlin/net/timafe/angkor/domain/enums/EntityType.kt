package net.timafe.angkor.domain.enums

import net.timafe.angkor.config.annotations.ManagedEntity
import java.util.*

/**
 * The very important and much refactored EntityType enum
 */
enum class EntityType(val path: String) {

    Area("areas"),
    Dish("dishes"),
    Event("events"),
    Feed("feeds"), // prefix (still) /links
    Link("links"),
    Note("notes"),
    Place("places"), // First Candidate for new Titlecase format !!!
    Tour("tours"), // prefix (still) /links
    User("users"),
    Photo("photos"), // Deliberately mixed case
    Post("posts"),
    Video("videos"); // prefix (still) /links

    /**
     *  // Todo should be obsolete now since all values are titlecase anyway
     *  simply capitalizes the name
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

        /**
         * Case-insensitive lookup for EntityType base on SimpleName of the class
         */
        fun <T> fromEntityClass(entityClass: Class<T>): EntityType {
            for (en in values()) {
                // todo this should be stricter (equals ignoring case), it's only done this way to temporary support
                // two Place classes
                if (entityClass.simpleName.lowercase().startsWith(en.name.lowercase())) {
                    return en
                }
            }
            throw IllegalArgumentException("cannot derive any entityType from class $entityClass")
        }

        fun fromEntityPath(path: String): EntityType {
            val normalizedPath = path.replace("/", "")
            for (en in values()) {
                if (en.path == normalizedPath) {
                    return en
                }
            }
            throw IllegalArgumentException("cannot derive any entityType from path $path")
        }

    }
}
