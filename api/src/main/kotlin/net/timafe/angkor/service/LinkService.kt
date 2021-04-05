package net.timafe.angkor.service

import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.dto.FeedItem
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.LinkRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.URL
import java.util.*

/**
 * Service Implementation for managing [Link].
 */
@Service
@Transactional
class LinkService(
    private val repo: LinkRepository
) : EntityService<Link, Link, UUID>(repo) {

    companion object {
        const val FEED_CACHE: String = "feedCache"
    }

    @Transactional(readOnly = true)
    fun findAllVideos(): List<Link> = repo.findAllVideos()

    @Transactional(readOnly = true)
    fun findAllFeeds(): List<Link> = repo.findAllFeeds()

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
        feed.entries.forEach { syndEntry ->
            jsonItems.add(
                FeedItem(
                    id = syndEntry.uri,
                    title = syndEntry.title,
                    url = syndEntry.link,
                    thumbnail = extractThumbnail(syndEntry)?.toString(),
                    summary = syndEntry.description?.value ?: "no description"
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

    override fun entityType(): EntityType {
        return EntityType.LINK
    }

}
