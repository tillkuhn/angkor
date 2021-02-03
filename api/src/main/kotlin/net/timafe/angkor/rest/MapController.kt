package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.security.AuthService
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_LATEST)
class MapController(
    private val repo: PlaceRepository,
    var authService: AuthService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/pois")
    fun getPOIs(): List<POI> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val items = repo.findPointOfInterests(authScopes)
        log.info("allPOIS return ${items.size} items authScopes=${authScopes}")
        return items.filter { it.getCoordinates().size > 1 }
    }
}
