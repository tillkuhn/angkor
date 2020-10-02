package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.repo.PlaceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION)
class MapController {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @GetMapping("/pois")
    fun getCoordinates(): List<POI> {
        log.debug("REST request to get all coordinates")
        val pois = placeRepository.findPointOfInterests()
        return pois.filter { it.coordinates != null && it.coordinates.size > 1 }
    }
}
