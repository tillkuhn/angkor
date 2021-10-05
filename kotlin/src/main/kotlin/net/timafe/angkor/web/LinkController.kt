package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.enums.LinkMediaType
import net.timafe.angkor.service.LinkService
import net.timafe.angkor.web.vm.ListItem
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * REST controller for managing [Link]s and related entities
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/links")
class LinkController(
    private val service: LinkService,
) : AbstractEntityController<Link, Link, UUID>(service) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun mergeUpdates(currentItem: Link, newItem: Link): Link =
        currentItem
            .copy(
                name = newItem.name,
                linkUrl = newItem.linkUrl,
                authScope = newItem.authScope,
                mediaType = newItem.mediaType,
                coordinates = newItem.coordinates
            )

    @GetMapping
    fun getLinks(): List<Link> {
        val items = service.findAll()
        log.info("getLinks returned ${items.size} links")
        return items
    }

    @GetMapping("/videos") // sub path /api/v1/links/videos
    fun getVideos(): List<Link> {
        val items = service.findByMediaType(LinkMediaType.VIDEO)
        log.info("getVideos returned ${items.size} videos")
        return items
    }

    @GetMapping("/komoot-tours") // sub path /api/v1/links/videos
    fun getKomootTours(): List<Link> {
        val items = service.findByMediaType(LinkMediaType.KOMOOT_TOUR)
        log.info("getKomootTours returned ${items.size} tours")
        return items
    }

    @GetMapping("/feeds") // sub path /api/v1/links/feeds
    fun getFeeds(): List<Link> {
        val items = service.findByMediaType(LinkMediaType.FEED)
        log.info("getFeeds returned ${items.size} feeds")
        return items
    }

    @GetMapping("/feeds/{id}") // sub path /api/v1/links/feeds
    fun getFeed(@PathVariable id: UUID): Feed {
        // https://timafe.wordpress.com/feed/
        return service.getFeed(id)
    }

    @GetMapping("/media-types") // sub path /api/v1/links/media-types
    fun getLinkMediaTypes(): List<ListItem> {
        return LinkMediaType.values()
            .sortedBy{it.label}
            .map { mt -> ListItem(mt.name, mt.label, mt.icon) }
    }

}
