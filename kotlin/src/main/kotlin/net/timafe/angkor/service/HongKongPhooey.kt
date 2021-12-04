package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.BulkResult
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.persistence.EntityManager


/**
 * Our Janitor Class for regular tasks such as event cleanup
 */
@Service
class HongKongPhooey(val entityManager: EntityManager) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 600 = 10 min, 3600 = 1h, 86400 = 1day (using seconds, 43200 = 12h default is millis)
    // https://www.baeldung.com/spring-scheduled-tasks#parameterizing-the-schedule
    @Scheduled(
        fixedRateString = "\${app.janitor.fixed-rate-seconds}",
        initialDelayString = "\${app.janitor.fixed-delay-seconds}",
        timeUnit = TimeUnit.SECONDS,
    )
    fun schedule() {
        this.cleanupEvents()
    }

    @Transactional
    fun cleanupEvents(): BulkResult {
        val deleteCount: Int =
            // https://vladmihalcea.com/bulk-update-delete-jpa-hibernate/
            entityManager.createQuery("""
        delete from Event 
        where topic = :topic and action = :action and time <= :validityThreshold""")
                .setParameter("topic", "system")
                .setParameter("action", "alert:unavailable")
                .setParameter("validityThreshold", ZonedDateTime.now().minusDays(100))
                .executeUpdate()
        log.info("[HongKongPhooey] Cleanup $deleteCount Events ")
        return BulkResult()
    }

}
