package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.BulkResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Our Janitor Class for regular tasks such as event cleanup
 */
@Service
class HongKongPhooeyService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun cleanupEvents(): BulkResult {
        log.info("[HongKongPhooey] Cleanup Events coming soon")
        return BulkResult()
    }

}
