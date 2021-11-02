package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Video
import net.timafe.angkor.service.VideoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(Constants.API_LATEST + "/videos")
class VideoController(
    private val service: VideoService
) : AbstractEntityController<Video, Video, UUID>(service) { // no Summary yet!

    @GetMapping
    fun findAll(): List<Video> {
        return service.findAll()
    }

    override fun mergeUpdates(currentItem: Video, newItem: Video): Video =
        currentItem.apply {
            // rating = newItem.rating
            name = newItem.name
            primaryUrl = newItem.primaryUrl
            authScope = newItem.authScope
            coordinates = newItem.coordinates
            tags = newItem.tags
        }
}
