package net.timafe.angkor.domain

import java.util.*
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

/**
 * New superclass for all Entities that use pre-generated UUIDs
 */
@MappedSuperclass
abstract class AbstractBaseEntity(
    // needs "open" or we get: Getter methods of lazy classes cannot be final
    @Id
    open var id: UUID = UUID.randomUUID(), // givenId ?: UUID.randomUUID()
) {

    // Using id for equals and hashcode is safe here since we control the UUID,
    // so it's initialized by the entity before it is persisted, and doesn't change after persist
    // Inspired by: https://jivimberg.io/blog/2018/11/05/using-uuid-on-spring-data-jpa-entities/
    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is AbstractBaseEntity -> false
            else -> id == other.id
        }
    }

    override fun toString() = "${this::class.simpleName}{" + "id='" + id + '\'' + "}"
}
