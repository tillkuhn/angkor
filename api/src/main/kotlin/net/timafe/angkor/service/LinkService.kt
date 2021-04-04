package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.dto.Feed
import net.timafe.angkor.domain.dto.FeedItem
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.LinkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    // Todo handle regular expiry
    @Cacheable(cacheNames = [FEED_CACHE])
    fun getFeed(id: UUID): Feed {
        val feedUrl = repo.findAllFeeds().firstOrNull{ it.id == id}?.linkUrl
            ?: throw EntityNotFoundException("No feed found for is ${id}")
        val input = SyndFeedInput()
        log.info("Loading feedUrl $feedUrl")
        val feed: SyndFeed = input.build(XmlReader( URL(feedUrl) ))
        // val feed: SyndFeed = input.build(javaClass.getResourceAsStream("/testfeed.xml").bufferedReader()) //.readLines()
        val jsonItems = mutableListOf<FeedItem>()
        feed.entries.forEach { item ->
            jsonItems.add(FeedItem(id = item.uri,
                title = item.title,
                url = item.link,
                summary = item.description?.value ?: "no description") // description is of type SyndContent
            )
        }
        val jsonFeed = Feed(
            title = feed.title,
            author = "hase",
            description = feed.description,
            feedURL = feedUrl,
            homePageURL = feed.link,
            items = jsonItems
        )
        return jsonFeed
    }

    override fun entityType(): EntityType {
        return EntityType.LINK
    }

}
