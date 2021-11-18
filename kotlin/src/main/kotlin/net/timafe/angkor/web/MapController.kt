package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.LocationPOI
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.service.LocationSearchService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing Location POIs
 * to support Map Display
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/pois")
class MapController(
    private val service: LocationSearchService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("{entityPath}")
    fun getPOIs(@PathVariable entityPath: String): List<LocationPOI> {
        val entityType = EntityType.fromEntityPath(entityPath)
        val items = service.searchMapLocations(SearchRequest.fromEntityTypes(entityType))
        log.info("[POI] getPOIs discovered ${items.size} interesting points for $entityType")
        return items.filter { it.coordinates.size > 1 } // requires at least LON / LAT
    }

}
