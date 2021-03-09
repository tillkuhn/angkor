package net.timafe.angkor.service

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.repo.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.persistence.PostPersist


//
/**
 * Entity Lister that automatically creates event records for supported entities
 * Note that even though it has no Spring Annotations
 *
 * https://stackoverflow.com/questions/12155632/injecting-a-spring-dependency-into-a-jpa-entitylistener
 * Hacks with @Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
 * Are no longer necessary with recent spring-boot / hibernate versions
 */
open class EntityEventListener {

    private val log = LoggerFactory.getLogger(javaClass)

    // for some reason we can't inject EventRepository directly in this special case
    // But we can inject applicationContext, and retrieve it programatically later
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @PostPersist
    // RequiresNew is mandatory, or you get concurrent modification exception at runtikme
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun onPostPersist(myEntity: Any) {
        log.debug("onPostPersist(): $myEntity")
        if (myEntity is EventSupport) {
            val createEvent = Event(
                entityType = myEntity.entityType(),
                entityId = myEntity.id,
                eventType = net.timafe.angkor.domain.enums.EventType.CREATED,
                summary = "${myEntity.entitySummary()} created",
                authScope = myEntity.authScope // Event should inherit auscope from parent entity
            )
            // Why like this? See comment on autowired ApplicationContext
            val er: EventRepository = applicationContext.getBean(EventRepository::class.java)
            er.save(createEvent)
        } else {
            log.warn("${myEntity.javaClass} does implement EventSupport")
        }
    }
}
