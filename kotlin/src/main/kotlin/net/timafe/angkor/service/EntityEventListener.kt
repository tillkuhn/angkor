@file:Suppress("SpringJavaAutowiredMembersInspection")

package net.timafe.angkor.service

import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.domain.enums.EventType
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.security.SecurityAuditorAware
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate


/**
 * Entity Lister that automatically creates event records for supported entities
 * Note that even though it has no Spring Annotations, Spring DI does work do some degree
 *
 * https://stackoverflow.com/questions/12155632/injecting-a-spring-dependency-into-a-jpa-entitylistener
 * Hacks with @Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
 * Are no longer necessary with recent spring-boot / hibernate versions
 *
 * Supported Lifecycle Events: https://www.baeldung.com/jpa-entity-lifecycle-events
 */
open class EntityEventListener {

    private val log = LoggerFactory.getLogger(javaClass)

    // We *can* inject applicationContext, and retrieve it programmatically later
    // but for some reason we can't inject EventRepository directly in this special class
    // See also Comments on Class Level
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    /**
     * Specifies a callback method for the corresponding lifecycle event "Persist" (new entity)
     */
    @PostPersist
    // still true? RequiresNew is mandatory to insert Event, or you get concurrent modification exception at runtime
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostPersist(entity: Any) {
        log.debug("[PostPersist] $entity")
        // Why retrieve via appContext? See comment on autowired ApplicationContext
        if (entity is EventSupport && typeInfo(entity)?.eventOnCreate == true) {
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, createEntityEvent(EventType.CREATE, entity))
        } else {
            log.warn("[PostPersist] ${entity.javaClass} skip creation of Persist Event")
        }
    }

    /**
     * Specifies a callback method for the corresponding lifecycle event "Update"
     */
    @PostUpdate
    open fun onPostUpdate(entity: Any) {
        log.debug("[PostUpdate] $entity")
        if (entity is EventSupport && typeInfo(entity)?.eventOnUpdate == true) {
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, createEntityEvent(EventType.UPDATE, entity))
        } else {
            log.warn("[PostUpdate] ${entity.javaClass} skip creation of Update Event")
        }
    }

    /**
     * Specifies a callback method for the corresponding lifecycle event "Remove"
     */
    @PostRemove
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostRemove(entity: Any) {
        log.debug("[PostRemove] $entity")
        if (entity is EventSupport && typeInfo(entity)?.eventOnDelete == true) {
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, createEntityEvent(EventType.DELETE, entity))
        } else {
            log.warn("[PostRemove] ${entity.javaClass} skip creation of Delete Event")
        }
    }

    /**
     * Helper method to create an Event with meaningful values
     */
    private fun createEntityEvent(eventType: EventType,entity: EventSupport): Event  {
        val saa = applicationContext.getBean(SecurityAuditorAware::class.java)
        val userId = if (saa.currentAuditor.isEmpty) null else saa.currentAuditor.get()
        val eType = EntityType.fromEntityClass(entity.javaClass)
        return Event(
            entityId = entity.id,
            userId = userId,
            action = "${eventType.actionPrefix}:${eType.name.lowercase()}", // e.g. create:place
            message = "${eventType.titlecase()} ${eType.name} ${entity.description()}",
        )
    }

    /**
     * Returns the @EntityTypeInfo runtime annotation if present on the Entity Class
     */
    private fun typeInfo(entity: Any): EntityTypeInfo? {
        val annotations = entity::class.annotations
        // https://stackoverflow.com/a/39806461/4292075
        return annotations.firstOrNull { it is EntityTypeInfo } as? EntityTypeInfo
    }

}
