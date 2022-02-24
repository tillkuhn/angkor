package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.service.TourService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for managing [Tour]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/tours")
class TourController(
    private val service: TourService
) : AbstractEntityController<Tour, Tour, UUID>(service) { // no TourSummary yet!

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    // not in base class (only search/)
    @GetMapping
    fun findAll(): List<Tour> {
        return service.findAll()
    }


    /**
     * Import a tour using the fully qualified external url, e.g.
     * https://api.externaltour.com/v1/tours/<tour-id>>?share_token=<share-token>
     */
    @PostMapping("/import")
    fun import(@RequestBody importRequest: ImportRequest): Tour {
        log.info("[Tour] Received ImportRequest $importRequest")
        return service.importExternal(importRequest)
    }

    /**
     * DEPRECATED Load tour by external Tour ID, probably no longer in use
     */
    @GetMapping("/external/{id}")
    fun loadExternal(@PathVariable id: Int): Tour {
        return service.importExternal(id)
    }

    override fun mergeUpdates(currentItem: Tour, newItem: Tour): Tour =
        currentItem.apply {
            rating = newItem.rating
        }
}
