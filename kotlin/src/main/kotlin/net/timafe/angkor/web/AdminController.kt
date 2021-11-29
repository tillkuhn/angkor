package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.service.HongKongPhooeyService
import net.timafe.angkor.service.PhotoService
import net.timafe.angkor.service.TourService
import net.timafe.angkor.web.vm.BooleanResult
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
    private val janitorService: HongKongPhooeyService,
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    enum class AdminAction(val description: String) {
        IMPORT_PHOTOS("Import Photos from external Service"),
        IMPORT_TOURS("Import Tours from external Service"),
        IMPORT_POSTS("Import Posts from external Blog"),
        CLEANUP_EVENTS("Cleanup Events"),
    }

    @PostMapping("/actions/{action}")
    @ResponseStatus(HttpStatus.OK)
    fun invokeAction(@PathVariable action: AdminAction): BooleanResult {
        log.info("[Admin] Received call for $action")
        when (action) {
            AdminAction.IMPORT_PHOTOS -> photoService.import()
            AdminAction.IMPORT_TOURS -> tourService.import()
            AdminAction.IMPORT_POSTS -> log.info("Importing Posts (to be implemented soon")
            AdminAction.CLEANUP_EVENTS -> janitorService.cleanupEvents()
            // else -> throw IllegalArgumentException("$action not supported here")
        }
        return BooleanResult(true)
    }

    @GetMapping("/actions")
    fun allActions(): Map<String,String> {
        // "associate" is short for map { it.name to it.description}.toMap()
        // https://www.baeldung.com/kotlin/list-to-map#implementation
        return AdminAction.values().associate { it.name to it.description }
    }

}
