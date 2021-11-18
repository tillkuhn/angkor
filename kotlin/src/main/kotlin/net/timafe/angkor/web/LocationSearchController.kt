package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.LocationSummary
import net.timafe.angkor.domain.dto.LocationPOI
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.service.LocationSearchService
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping(Constants.API_LATEST+"/locations")
class LocationSearchController(private val service: LocationSearchService) {

    // private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping("search/")
    fun searchAll(): List<LocationSummary> = search(SearchRequest())

    @GetMapping("search/{query}")
    fun search(@PathVariable query: String): List<LocationSummary> = search(SearchRequest(query=query))

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<LocationSummary> {
        return service.search(search)
    }

    /**
     * Experimental POI Projection
     */
    @PostMapping("pois")
    fun searchMapLocations(@Valid @RequestBody search: SearchRequest): List<LocationPOI> {
        return service.searchMapLocations(search)
    }



}
