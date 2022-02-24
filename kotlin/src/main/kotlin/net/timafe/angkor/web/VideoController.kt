package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.service.VideoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for managing [Video]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/videos")
class VideoController(
    private val service: VideoService
) : AbstractEntityController<Video, Video, UUID>(service) { // no Summary yet!

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    // not in base class (only search/)
    @GetMapping
    fun findAll(): List<Video> {
        return service.findAll()
    }

    override fun mergeUpdates(currentItem: Video, newItem: Video): Video =
        currentItem.apply {
            name = newItem.name
            primaryUrl = newItem.primaryUrl
            authScope = newItem.authScope
            coordinates = newItem.coordinates
            tags = newItem.tags
        }

    /**
     * Import a video using the fully qualified external url, e.g.
     * https://senftu.be/12AB_aabb
     */
    @PostMapping("/import")
    fun import(@RequestBody importRequest: ImportRequest): Video {
        log.info("[Video] Received ImportRequest $importRequest")
        return service.importExternal(importRequest)
    }
}
