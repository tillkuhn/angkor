package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.service.PlaceService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * REST controller for managing [Place].
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/" + Constants.API_PATH_PLACES)
class PlaceController(
    var service: PlaceService,
) : EntityController<Place, PlaceSummary, UUID>(service) {

    override fun mergeUpdates(currentItem: Place, newItem: Place): Place =
        currentItem
            .copy(
                name = newItem.name,
                summary = newItem.summary,
                notes = newItem.notes,
                locationType = newItem.locationType,
                areaCode = newItem.areaCode,
                primaryUrl = newItem.primaryUrl,
                imageUrl = newItem.imageUrl,
                coordinates = newItem.coordinates,
                authScope = newItem.authScope,
                tags = newItem.tags
            )


}
