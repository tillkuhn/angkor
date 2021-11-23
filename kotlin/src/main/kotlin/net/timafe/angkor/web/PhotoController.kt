package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Photo
import net.timafe.angkor.domain.Video
import net.timafe.angkor.service.PhotoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * REST controller for managing [Video]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/photos")
class PhotoController(
    private val service: PhotoService
) : AbstractEntityController<Photo, Photo, UUID>(service) { // no Summary yet!

    // not in base class (only search/)
    @GetMapping
    fun findAll(): List<Photo> {
        return service.findAll()
    }

    override fun mergeUpdates(currentItem: Photo, newItem: Photo): Photo =
        currentItem.apply {
            // rating = newItem.rating
            name = newItem.name
            primaryUrl = newItem.primaryUrl
            authScope = newItem.authScope
            coordinates = newItem.coordinates
            tags = newItem.tags
        }
}
