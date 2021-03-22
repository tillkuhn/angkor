package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.service.LinkService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * REST controller for managing [POI]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/links")
class LinkController(
    private val service: LinkService,
) : EntityController<Link, Link, UUID>(service) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun mergeUpdates(currentItem: Link, newItem: Link): Link =
        currentItem
            .copy(
                name = newItem.name,
                linkUrl = newItem.linkUrl,
                authScope = newItem.authScope,
                mediaType = newItem.mediaType,
            )

    @GetMapping
    fun getLinks(): List<Link> {
        val items = service.findAll()
        log.info("getVideos return ${items.size} links")
        return items
    }

    @GetMapping("/videos") // sub path /api/v1/links/videos
    fun getVideos(): List<Link> {
        val items = service.findAllVideos()
        log.info("getVideos return ${items.size} videos")
        return items
    }

    @GetMapping("/feeds") // sub path /api/v1/links/feeds
    fun getFeeds(): List<Link> {
        val items = service.findAllFeeds()
        log.info("getFeeds return ${items.size} feeds")
        return items
    }

}