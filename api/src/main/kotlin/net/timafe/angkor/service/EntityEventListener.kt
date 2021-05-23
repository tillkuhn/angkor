@file:Suppress("SpringJavaAutowiredMembersInspection")

package net.timafe.angkor.service

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.dto.EventMessage
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.domain.enums.EventType
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.repo.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.persistence.PostPersist
import javax.persistence.PostRemove


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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostPersist(ente: Any) {
        log.debug("onPostPersist(): $ente")
        if (ente is EventSupport) {
            // Why like this? See comment on autowired ApplicationContext
            val er: EventRepository = applicationContext.getBean(EventRepository::class.java)
            val event = entityEvent(ente, EventType.CREATED)
            er.save(event)
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, eventMessage("create",event))
        } else {
            log.warn("${ente.javaClass} does implement EventSupport, skip creation of Persist Event")
        }
    }

    @PostRemove
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostRemove(ente: Any) {
        log.debug("onPostRemove(): $ente")
        if (ente is EventSupport) {
            val er: EventRepository = applicationContext.getBean(EventRepository::class.java)
            val event = entityEvent(ente, EventType.DELETED)
            er.save(event)
            val es: EventService = applicationContext.getBean(EventService::class.java)
            es.publish(EventTopic.APP, eventMessage("delete",event))
        } else {
            log.warn("${ente.javaClass} does implement EventSupport, skip creation of Remove Event")
        }
    }

    private fun entityEvent(ente: EventSupport, eventType: EventType): Event = Event(
        entityType = EntityType.fromEntityAnnotation(ente),
        entityId = ente.id,
        eventType = eventType,
        summary = "${ente.description()} ${eventType.verb}",
        authScope = ente.authScope // Event should inherit auth scope from parent entity
    )

    private fun eventMessage(actionPrefix: String, event: Event): EventMessage {
        val action = "${actionPrefix}:${event.entityType?.name?.toLowerCase()}"
        return EventMessage(action = action, message = event.summary, source = this.javaClass.simpleName)
    }
}
