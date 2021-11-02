package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.ExternalTour
import net.timafe.angkor.service.TourService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(Constants.API_LATEST + "/tours")
class TourController(
    private val service: TourService
) : AbstractEntityController<Tour, Tour, UUID>(service) { // no TourSummary yet!

    // not in base class (only search/)
    @GetMapping
    fun findAll(): List<Tour> {
        return service.findAll()
    }

    @GetMapping("/external/{id}")
    fun loadExternal(@PathVariable id: Int): ExternalTour {
        return service.loadSingleExternalTour(id)
    }

    override fun mergeUpdates(currentItem: Tour, newItem: Tour): Tour =
        currentItem.apply {
            rating = newItem.rating
        }
}
