package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.ExternalTour
import net.timafe.angkor.service.TourService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_LATEST + "/tours")
class TourController(private val service: TourService) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/external/{id}")
    fun loadExternal(@PathVariable id: Int): ExternalTour {
        return service.loadExternal(id)
    }
}
