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
import java.time.Duration
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
    @Value("\${app.janitor.days-to-keep-audit-events}")
    private val daysToKeepAuditEvents: Int,
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
        this.cleanupEventsLoop()
    }

    @Transactional
    fun cleanupEventsLoop(): BulkResult {
        val totalEvents = entityManager.createQuery("""SELECT COUNT(e) FROM Event e""".trimMargin()).singleResult
        val result = BulkResult()

        // validityThreshold derived from app properties, something similar to 91 days
        val validityThreshold = ZonedDateTime.now().minusDays(daysToKeepSystemEvents.toLong())
        val validityThresholdAudit = ZonedDateTime.now().minusDays(daysToKeepAuditEvents.toLong())

        // fine-grained calls for particular actions
        result.add(cleanupEvents("system", "alert:", validityThreshold))
        result.add(cleanupEvents("system", "runjob:", validityThreshold))
        result.add(cleanupEvents("audit", "login:", validityThresholdAudit))
        result.add(cleanupEvents("audit", "logout:", validityThresholdAudit))

        val msg = "Hong Kong Phooey cleaned up ${result.deleted}/$totalEvents system events kept for $daysToKeepSystemEvents days"
        log.info("[Janitor] $msg")
        val auth = SecurityContextHolder.getContext().authentication
        var eventUserId: UUID? = null
        if (auth is ServiceAccountToken) {
            eventUserId = auth.id
        }
        if (result.deleted > 0) {
            val event = Event(action = "cleanup:events", message = msg, userId = eventUserId)
            eventService.publish(EventTopic.SYSTEM, event)
        }
        return result
    }

    @Transactional
    fun cleanupEvents(topic: String, actionContains: String, validityThreshold: ZonedDateTime): BulkResult {
        // Bulk Update and Delete with JPA and Hibernate
        // https://vladmihalcea.com/bulk-update-delete-jpa-hibernate/
        // why "LIKE" this :-)? https://stackoverflow.com/a/48926585/4292075
        val deleteCount = entityManager.createQuery(
            """
                DELETE FROM Event 
                WHERE topic = :topic AND action LIKE CONCAT('%',:actionLike,'%') AND time <= :validityThreshold
                """
        )
            .setParameter("topic", topic)
            .setParameter("actionLike", actionContains)
            .setParameter("validityThreshold", validityThreshold) // validityThreshold is a ZonedDateTime
            .executeUpdate()
        val days = Duration.between(validityThreshold, ZonedDateTime.now()).toDays()
        log.debug("[Janitor] Delete topic=$topic actionContains=$actionContains days=$days: $deleteCount records")
        return BulkResult(deleted = deleteCount)
    }

}

// Top Events
//login:user,audit,346
//runjob:remindabot,system,276
//runjob:certbot,system,267
//create:mail,system,211
//runjob:backup-db,system,191
//runjob:backup-s3,system,184
//create:image,app,177
//startup:imagine,system,152
//startup:healthbells,system,144
//update:dish,app,143
//startup:angkor-api,system,138
//startup:polly,system,136
//update:place,app,122
//deploy:docs,system,119
//serve:dish,app,90
//update:note,app,82
//create:place,app,60
//create:note,app,55
//create:photo,app,53
//logout:user,audit,37
//
