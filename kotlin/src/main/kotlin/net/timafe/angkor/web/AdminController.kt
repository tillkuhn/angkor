package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.service.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Admin Actions
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/" + Constants.API_PATH_ADMIN)
class AdminController(
    private val photoService: PhotoService,
    private val tourService: TourService,
    private val postService: PostService,
    private val janitorService: HongKongPhooey,
    private val cacheService: CacheService,
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    enum class AdminAction(val description: String) {
        IMPORT_PHOTOS("Import Photos from external Service"),
        IMPORT_TOURS("Import Tours from external Service"),
        IMPORT_POSTS("Import Posts from external Blog"),
        CLEANUP_EVENTS("Cleanup Events"),
        EVICT_ALL_CACHES("Evict all caches "),
    }

    @PostMapping("/actions/{action}")
    @ResponseStatus(HttpStatus.OK)
    fun invokeAction(@PathVariable action: AdminAction): BulkResult {
        log.info("[Admin] Received call for $action")

        val bulkResult = when (action) {
            AdminAction.IMPORT_PHOTOS -> photoService.import()
            AdminAction.IMPORT_TOURS -> tourService.import()
            AdminAction.IMPORT_POSTS -> postService.import()
            AdminAction.CLEANUP_EVENTS -> janitorService.cleanupEvents()
            AdminAction.EVICT_ALL_CACHES -> cacheService.evictAllCaches()
            // else -> throw IllegalArgumentException("$action not supported here")
        }
        return bulkResult
    }

    @GetMapping("/actions")
    fun allActions(): Map<String,String> {
        // "associate" is short for map { it.name to it.description}.toMap()
        // https://www.baeldung.com/kotlin/list-to-map#implementation
        return AdminAction.values().associate { it.name to it.description }
    }

}
