package net.timafe.angkor.service

import com.rometools.modules.georss.GeoRSSModule
import com.rometools.modules.georss.GeoRSSUtils
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.dto.FeedItem
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.Media_Type
import net.timafe.angkor.repo.LinkRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.net.URI
import java.util.*


/**
 * Service Implementation for managing [Link].
 */
@Service
@Transactional
class LinkService(
    private val repo: LinkRepository,
) : AbstractEntityService<Link, Link, UUID>(repo) {

    companion object {
        const val FEED_CACHE: String = "feedCache"
    }

    // Try generic method instead
    @Transactional(readOnly = true)
    fun findByMediaType(mediaType: Media_Type): List<Link> = repo.findByMediaType(mediaType)

    // Todo handle regular expiry
    @Cacheable(cacheNames = [FEED_CACHE])
    fun getFeed(id: UUID): Feed {
        val feedUrl = repo.findAllFeeds().firstOrNull { it.id == id }?.linkUrl
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No feed found for is $id")
        // val input = SyndFeedInput()
        log.info("Loading feedUrl $feedUrl")
        // URL arg for XmlReader is deprecated, see https://github.com/rometools/rome/issues/276
        val restTemplate = RestTemplate()
        val feed = restTemplate.execute<SyndFeed>(feedUrl, HttpMethod.GET, null,
            { response: ClientHttpResponse ->
                val input = SyndFeedInput()
                try {
                    // In Kotlin, the return@label syntax is used for specifying which function among several nested
                    // ones this statement returns from. https://stackoverflow.com/a/40166597/4292075
                    return@execute input.build(XmlReader(response.body))
                } catch (e: FeedException) {
                    throw IOException("Could not parse feed response, root cause: " + e.message)
                }
            })
        //val feed: SyndFeed = input.build(XmlReader(URL(feedUrl)))
        // val feed = input.build(javaClass.getResourceAsStream("/test-feed.xml").bufferedReader()) //.readLines()
        if (feed == null) {
            throw IOException("Feed is null for $feedUrl")
        }
        val jsonItems = mutableListOf<FeedItem>()
        feed.entries.forEach { entry ->
            jsonItems.add(
                FeedItem(
                    id = entry.uri,
                    title = entry.title,
                    url = entry.link,
                    thumbnail = extractThumbnail(entry)?.toString(),
                    description = entry.description?.value ?: "no description",
                    coordinates = extractCoordinates(entry)
                ), // description is of type SyndContent
            )
        }
        log.debug("$feedUrl returned ${jsonItems.size} lines")

        return Feed(
            title = feed.title,
            author = "hase",
            description = feed.description,
            feedURL = feedUrl,
            homePageURL = feed.link,
            items = jsonItems
        )
    }

    // handle <media:thumbnail url="https://timafe.files.wordpress.com/2021/01/echse.jpg" />
    fun extractThumbnail(entry: SyndEntry): URI? {
        for (module in entry.modules) {
            if (module is MediaEntryModule) {
                for (thumb in module.metadata.thumbnail) {
                    log.trace("Found thumb {} in {}", thumb.url, entry.link)
                    return thumb.url
                }
            }
        }
        return null
    }

    // https://rometools.github.io/rome/Modules/GeoRSS.html
    fun extractCoordinates(entry: SyndEntry): List<Double> {
        val geoRSSModule: GeoRSSModule? = GeoRSSUtils.getGeoRSS(entry)
        if (geoRSSModule?.position != null) {
            log.trace("pos = {}", geoRSSModule.position)
            return listOf(geoRSSModule.position.longitude, geoRSSModule.position.latitude)
        }
        return listOf()
    }

    override fun entityType(): EntityType {
        return EntityType.Link
    }

}
