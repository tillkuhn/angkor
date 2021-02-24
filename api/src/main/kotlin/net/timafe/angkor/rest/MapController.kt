package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.service.PlaceService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing [POI]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class MapController(
    private val service: PlaceService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/pois")
    fun getPOIs(): List<POI> {
        val items = service.findPointOfInterests()
        log.info("allPOIS return ${items.size} items")
        return items.filter { it.getCoordinates().size > 1 }
    }

}
