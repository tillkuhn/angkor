package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.service.PhotoService
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
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    enum class AdminAction {
        IMPORT_PHOTOS,
        IMPORT_TOURS,
    }

    @PostMapping("/actions/{action}")
    @ResponseStatus(HttpStatus.OK)
    fun invokeAction(@PathVariable action: AdminAction): BooleanResult {
        log.info("[Admin] Received call for $action")
        when (action) {
            AdminAction.IMPORT_PHOTOS -> {
                log.info("Importing Photos")
                photoService.import()
            }
            AdminAction.IMPORT_TOURS -> log.info("Importing Tours (to be implemented soon")
            // else -> throw IllegalArgumentException("$action not supported here")
        }
        return BooleanResult(true)
    }


}
