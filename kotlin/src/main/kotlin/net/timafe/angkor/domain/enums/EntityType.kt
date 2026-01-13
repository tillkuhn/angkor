package net.timafe.angkor.domain.enums

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

    companion object {

        /**
         * Case-insensitive lookup for EntityType base on SimpleName of the class
         */
        fun <T> fromEntityClass(entityClass: Class<T>): EntityType {
            for (en in entries) {
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
            for (en in entries) {
                if (en.path == normalizedPath) {
                    return en
                }
            }
            throw IllegalArgumentException("cannot derive any entityType from path $path")
        }

    }
}
