package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.repo.LinkRepository
import net.timafe.angkor.service.PlaceService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing [POI]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class LinkController(
    private val repo: LinkRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/videos")
    fun getVideos(): List<Link> {
        val items = repo.findAllVideos()
        log.info("getVideos return ${items.size} items")
        return items
    }

}
