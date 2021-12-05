package net.timafe.angkor.service

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.security.ServiceAccountToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.EntityManager

/**
 * Our Janitor Class for regular tasks such as event cleanup
 */
@Service
class HongKongPhooey(
    val entityManager: EntityManager,
    val userService: UserService,
    val eventService: EventService,
    @Value("\${app.janitor.days-to-keep-system-events}")
    private val daysToKeepSystemEvents: Int,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 600 = 10 min, 3600 = 1h, 86400 = 1day (using seconds, 43200 = 12h default is millis)
    // https://www.baeldung.com/spring-scheduled-tasks#parameterizing-the-schedule
    @Scheduled(
        fixedRateString = "\${app.janitor.fixed-rate-seconds}",
        initialDelayString = "\${app.janitor.fixed-delay-seconds}",
        timeUnit = TimeUnit.SECONDS,
    )
    @Transactional
    fun schedule() {
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
        this.cleanupEvents()
    }

    @Transactional
    fun cleanupEvents(): BulkResult {
        // https://vladmihalcea.com/bulk-update-delete-jpa-hibernate/
        val totalEvents = entityManager.createQuery("""SELECT COUNT(e) FROM Event e""".trimMargin()).singleResult
        val validityThreshold = ZonedDateTime.now().minusDays(daysToKeepSystemEvents.toLong())
        // why "LIKE" this :-)? https://stackoverflow.com/a/48926585/4292075
        val deleteCount: Int = entityManager.createQuery("""
            DELETE FROM Event 
            WHERE topic = :topic AND action LIKE CONCAT('%',:actionLike,'%') AND time <= :validityThreshold
            """)
                .setParameter("topic", "system")
                .setParameter("actionLike", "alert:")
                .setParameter("validityThreshold",validityThreshold )
                .executeUpdate()
        val result = BulkResult(deleted = deleteCount)
        val msg = "Hong Kong Phooey cleaned up $deleteCount of $totalEvents system events kept for ${daysToKeepSystemEvents} days"
        log.info("[Janitor] $msg")
        val auth = SecurityContextHolder.getContext().authentication
        var eventUserId: UUID? = null
        if (auth is ServiceAccountToken) {
            eventUserId = auth.id
        }
        if (deleteCount > 0) {
            val event = Event(action = "cleanup:events", message = msg, userId = eventUserId)
            eventService.publish(EventTopic.SYSTEM, event)
        }
        return result
    }

}
