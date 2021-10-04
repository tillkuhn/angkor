package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.ExternalTour
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.service.TourService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Constants.API_LATEST + "/tours")
class TourController(
    private val service: TourService
) : AbstractEntityController<Tour, Tour, UUID>(service) { // no TourSummary yet!

    @GetMapping("/external/{id}")
    fun loadExternal(@PathVariable id: Int): ExternalTour {
        return service.loadSingleExternalTour(id)
    }

    override fun mergeUpdates(currentItem: Tour, newItem: Tour): Tour {
        TODO("Not yet implemented")
    }

    // TODO our query does not support search requests with sort etc. yet
    // so we delegate to searchAll which uses an empty SearchRequest
//    @PostMapping("search")
//    override fun search(search: SearchRequest): List<Tour> {
//        return service.search(SearchRequest())
//    }
}
