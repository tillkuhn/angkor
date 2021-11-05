package net.timafe.angkor.service

import com.rometools.modules.georss.GeoRSSModule
import com.rometools.modules.georss.GeoRSSUtils
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.dto.FeedItem
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LinkMediaType
import net.timafe.angkor.repo.LinkRepository
import org.jdom2.Content
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.isDirectory


/**
 * Service Implementation for managing [Link].
 */
@Service
@Transactional
class LinkService(
    private val repo: LinkRepository,
    @Value("\${user.home}/.angkor/import") private val importFolder: String
) : AbstractEntityService<Link, Link, UUID>(repo) {

    companion object {
        const val FEED_CACHE: String = "feedCache"
    }

    @Scheduled(fixedRateString = "43200", initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    @Transactional
    fun import() {
        val importPath = Paths.get(importFolder)
        if (!importPath.isDirectory()) {
            log.warn("[FeedImporter] ImportFolder $importFolder does not exist (or is not a directory)")
            return
        }
        Files.walk(importPath)
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".xml") }
            .forEach { importXML(it) }
        log.info("[FeedImporter] Finished checking $importFolder from potential files")
    }

    fun importXML(item: Path) {
        log.info("Importing: $item") // item is full path
        val input = SyndFeedInput()
        val feed: SyndFeed = input.build(XmlReader(item.toFile()))
        val articles = mutableListOf<FeedItem>()
        feed.entries
            // Todo: check foreign markup list
            // media is also considered a feed entry. Better: Match <wp:post_type>attachment</wp:post_type or feedback vs post
            //.filter { entry -> ! (entry.uri.lowercase().endsWith(".jpg") || entry.uri.lowercase().endsWith(".png")) }
            .filter { entry -> isPost(entry) }
            .forEach { entry ->
                articles.add(
                    FeedItem(
                        id = entry.uri,
                        title = entry.title,
                        url = entry.link,
                        thumbnail = extractThumbnail(entry)?.toString(),
                        description = entry.description?.value ?: "no description",
                        coordinates = extractCoordinatesWP(entry)
                        // todo checkout ContentModule https://rometools.github.io/rome/Modules/Content.html
                    ), // description is of type SyndContent
                )
            }
        articles.forEach { entry ->
            log.info("Converted BlogPost to Article: $entry")
        }
    }

    private fun isPost(entry: SyndEntry): Boolean {
        val postTypeElements = entry.foreignMarkup
            .filter { ele -> ele.name.equals("post_type") }
            .filter { ele -> ele.content.size > 0 && ele.content[0].value.equals("post") }
        return postTypeElements.isNotEmpty()
    }

    private fun extractCoordinatesWP(entry: SyndEntry): List<Double> {
        val lat = extractMetaElementWP(entry, "geo_latitude")
        val lon = extractMetaElementWP(entry, "geo_longitude")
        if (lat != null && lon != null) {
            return listOf(lon, lat)
        }
        return listOf()
    }

    private fun extractMetaElementWP(entry: SyndEntry, name: String): Double? {
        val postMetaElements = entry.foreignMarkup
            .filter { ele -> ele.name.equals("postmeta") }
        for (m in postMetaElements) {
            val isTheOne = m.children
                .filter { it.name.equals("meta_key") }
                .any { it.content.size > 0 && it.content[0].value.equals(name) }
            if (isTheOne) {
                val cont = m.children
                    .first { it.name.equals("meta_value") }
                    .content.first { it.cType == Content.CType.CDATA }
                    .value
                return cont.toDouble()
            }
        }
        return null
    }

    // Try generic method instead
    @Transactional(readOnly = true)
    fun findByMediaType(mediaType: LinkMediaType): List<Link> = repo.findByMediaType(mediaType)

    // Todo handle regular expiry
    @Cacheable(cacheNames = [FEED_CACHE])
    fun getFeed(id: UUID): Feed {
        val feedUrl = repo.findAllFeeds().firstOrNull { it.id == id }?.linkUrl
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No feed found for is $id")
        val input = SyndFeedInput()
        log.info("Loading feedUrl $feedUrl")
        val feed: SyndFeed = input.build(XmlReader(URL(feedUrl)))
        // val feed = input.build(javaClass.getResourceAsStream("/test-feed.xml").bufferedReader()) //.readLines()
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
                    log.trace("Found thumb ${thumb.url} in ${entry.link}")
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
            log.trace("pos = ${geoRSSModule.position}")
            return listOf(geoRSSModule.position.longitude, geoRSSModule.position.latitude)
        }
        return listOf()
    }

    override fun entityType(): EntityType {
        return EntityType.LINK
    }

}
