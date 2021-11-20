@file:Suppress("SpringJavaAutowiredMembersInspection")

package net.timafe.angkor.service

import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.LocatableEntity
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.domain.enums.EventType
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.security.SecurityAuditorAware
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import java.util.*
import javax.persistence.PostPersist
import javax.persistence.PostRemove
import javax.persistence.PostUpdate


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
    // CHECK still true? RequiresNew is mandatory to insert Event, or you get concurrent modification exception at runtime
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostPersist(entity: Any) {
        log.debug("[PostPersist] $entity")
        // Why retrieve via appContext? See comment on autowired ApplicationContext
        val es: EventService = applicationContext.getBean(EventService::class.java)
        if (entity is EventSupport) {
            // Why don't we just inject? See comment on autowired ApplicationContext above!
            es.publish(EventTopic.APP, createEntityEvent(EventType.CREATE, entity))
        } else if (getEntityTypeInfo(entity)?.eventOnCreate == true) {
            log.debug("[PostUpdate] ${entity.javaClass} @${EntityTypeInfo::class.simpleName}.eventOnCreate=true")
            if (entity is LocatableEntity) {
                es.publish(EventTopic.APP, createEntityEvent(EventType.CREATE, entity))
            }
        } else {
            log.warn("[PostPersist] ${entity.javaClass} does not implement EventSupport, skip creation of Persist Event")
        }
    }

    /**
     * Specifies a callback method for the corresponding lifecycle event "Update"
     */
    @PostUpdate
    open fun onPostUpdate(entity: Any) {
        log.debug("[PostUpdate] $entity")
        val es: EventService = applicationContext.getBean(EventService::class.java)
        // Experiment: Using EntityType info should be the new way to do it, get rid of EventSupport interface
        if (entity is EventSupport) {
            es.publish(EventTopic.APP, createEntityEvent(EventType.UPDATE, entity))
        } else if (getEntityTypeInfo(entity)?.eventOnUpdate == true) {
            log.debug("[PostUpdate] ${entity.javaClass} @${EntityTypeInfo::class.simpleName}.eventOnUpdate=true")
            if (entity is LocatableEntity) {
                es.publish(EventTopic.APP, createEntityEvent(EventType.UPDATE, entity))
            }
        } else {
            log.warn("[PostUpdate] ${entity.javaClass} does implement EventSupport, skip creation of Update Event")
        }
    }

    /**
     * Specifies a callback method for the corresponding lifecycle event "Remove"
     */
    @PostRemove
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostRemove(entity: Any) {
        log.debug("[PostRemove] $entity")
        val es: EventService = applicationContext.getBean(EventService::class.java)
        if (entity is EventSupport) {
            es.publish(EventTopic.APP, createEntityEvent(EventType.DELETE, entity))
        } else if (getEntityTypeInfo(entity)?.eventOnDelete == true) {
            log.debug("[PostUpdate] ${entity.javaClass} @${EntityTypeInfo::class.simpleName}.eventOnDelete=true")
            if (entity is LocatableEntity) {
                es.publish(EventTopic.APP, createEntityEvent(EventType.DELETE, entity))
            }
        } else {
            log.warn("${entity.javaClass} does implement EventSupport, skip creation of Remove Event")
        }
    }

    /**
     * Helper method to create an Event with meaningful values
     */
    private fun createEntityEvent(eventType: EventType,entity: Any): Event  {
        var id: UUID?
        var desc: String
        if (entity is EventSupport) {
            id = entity.id
            desc = entity.description()
        } else if (entity is LocatableEntity) {
            id = entity.id
            desc = "${entity.name} (${entity.areaCode})"
        } else {
            throw IllegalArgumentException("${entity::class} not yet supported for entity events")
        }
        val saa = applicationContext.getBean(SecurityAuditorAware::class.java)
        val userId = if (saa.currentAuditor.isEmpty) null else saa.currentAuditor.get()
        val eType = EntityType.fromEntityClass(entity.javaClass)
        return Event(
            entityId = id,
            userId = userId,
            action = "${eventType.actionPrefix}:${eType.name.lowercase()}", // e.g. create:place
            message = "${eventType.titlecase()} ${eType.name} $desc",
        )
    }

    /**
     * Retuns the @EntityTypeInfo runtime annotation if present on the Entity Class
     */
    private fun getEntityTypeInfo(entity: Any): EntityTypeInfo? {
        val annotations = entity::class.annotations
        // https://stackoverflow.com/a/39806461/4292075
        return annotations.firstOrNull { it is EntityTypeInfo } as? EntityTypeInfo
    }

}
