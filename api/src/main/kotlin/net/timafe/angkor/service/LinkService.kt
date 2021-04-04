package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.dto.FeedItem
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.LinkRepository
import net.timafe.angkor.repo.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException
import java.net.URL
import java.util.*
import javax.persistence.EntityNotFoundException

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

    @Cacheable(cacheNames = [FEED_CACHE])
    fun getFeed(id: UUID): Feed {
        val feedUrl = repo.findAllFeeds().firstOrNull{ it.id == id}?.linkUrl
            ?: throw EntityNotFoundException("No feed found for is ${id}")
        val input = SyndFeedInput()
        log.info("Loading feedUrl $feedUrl")
        val feed: SyndFeed = input.build(XmlReader( URL(feedUrl) ))
        // val feed: SyndFeed = input.build(javaClass.getResourceAsStream("/testfeed.xml").bufferedReader()) //.readLines()
        val items = mutableListOf<FeedItem>()
        feed.entries.forEach { entry ->
            items.add(FeedItem(id = entry.uri, title = entry.title, url = entry.link))
        }
        val jsonFeed = Feed(
            title = feed.title,
            author = "hase", description = feed.description, feedURL = "url", homePageURL = "", items = items
        )
        return jsonFeed
    }

    override fun entityType(): EntityType {
        return EntityType.LINK
    }

}
