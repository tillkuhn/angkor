package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import javax.validation.Valid



@RestController
@RequestMapping(Constants.API_LATEST+"/locations")
class LocationController(private val service: LocationService) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping("search/")
    fun searchAll(): List<Location> = search(SearchRequest())

    @GetMapping("search/{query}")
    fun search(@PathVariable query: String): List<Location> = search(SearchRequest(query=query))


    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<Location> {
        return service.search(search)
    }


}
