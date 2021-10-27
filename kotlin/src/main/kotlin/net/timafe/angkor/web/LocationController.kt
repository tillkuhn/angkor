package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.repo.LocationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_LATEST)
class LocationController(
    private val repo: LocationRepository,
) {

    @GetMapping("/locations")
    fun findAll(): List<Location> = repo.findAll().toList()

}
