@file:Suppress("SpringJavaAutowiredMembersInspection")

package net.timafe.angkor.service

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.domain.enums.EventType
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.security.SecurityAuditorAware
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import javax.persistence.PostPersist
import javax.persistence.PostRemove
import javax.persistence.PostUpdate


/**
 * Entity Lister that automatically creates event records for supported entities
 * Note that even though it has no Spring Annotations
 *
 * https://stackoverflow.com/questions/12155632/injecting-a-spring-dependency-into-a-jpa-entitylistener
 * Hacks with @Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
 * Are no longer necessary with recent spring-boot / hibernate versions
 *
 * Supported Lifecycle Events: https://www.baeldung.com/jpa-entity-lifecycle-events
 */
open class EntityEventListener {

    private val log = LoggerFactory.getLogger(javaClass)

    // for some reason we can't inject EventRepository directly in this special case
    // But we can inject applicationContext, and retrieve it programmatically later
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    /**
     * Triggered after persist is called for a new entity – @PostPersist
     */
    @PostPersist
    // RequiresNew is mandatory to insert Event, or you get concurrent modification exception at runtime
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostPersist(ente: Any) {
        log.debug("[PostPersist] $ente")
        if (ente is EventSupport) {
            val event = createEntityEvent(ente, EventType.CREATE)
            // er.save(event)
            // Why like this? See comment on autowired ApplicationContext
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP,  event)
        } else {
            log.warn("${ente.javaClass} does implement EventSupport, skip creation of Persist Event")
        }
    }

    @PostUpdate
    open fun onPostUpdate(ente: Any) {
        log.debug("[PostUpdate] $ente")
        if (ente is EventSupport) {
            val event = createEntityEvent(ente, EventType.UPDATE)
            // Why like this? See comment on autowired ApplicationContext
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, event)
        } else {
            log.warn("${ente.javaClass} does implement EventSupport, skip creation of Persist Event")
        }

    }

    @PostRemove
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostRemove(ente: Any) {
        log.debug("[PostRemove] $ente")
        if (ente is EventSupport) {
            val event = createEntityEvent(ente, EventType.DELETE)
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, event)
        } else {
            log.warn("${ente.javaClass} does implement EventSupport, skip creation of Remove Event")
        }
    }

    private fun createEntityEvent(entity: EventSupport, eventType: EventType): Event  {
        val saa = applicationContext.getBean(SecurityAuditorAware::class.java)
        val userId = if (saa.currentAuditor.isEmpty) null else saa.currentAuditor.get()
        val eType = EntityType.fromEntityAnnotation(entity)
        return Event(
            entityId = entity.id,
            userId = userId,
            action = "${eventType.actionPrefix}:${eType.name.toLowerCase()}",
            message = "${eventType.actionPrefix.capitalize()} ${eType.name.toLowerCase().capitalize()} ${entity.description()}",
        )
    }

}
