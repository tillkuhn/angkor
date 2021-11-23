package net.timafe.angkor.service.utils

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

class FeedUtils {

    companion object {

        val log: Logger = LoggerFactory.getLogger(FeedUtils::class.java)

        fun <T> parseFeed(feedUrl: String, mapperFunc: (syndEntry: SyndEntry) -> T): List<T> {
            val input = SyndFeedInput()
            log.info("[Feeder] Loading feedUrl $feedUrl")
            val feed: SyndFeed = if (feedUrl.startsWith("https://")) input.build(XmlReader(URL(feedUrl)))
            else input.build(XmlReader(File(feedUrl))) //.readLines()

            val entities = mutableListOf<T>()
            feed.entries.forEach { feedItem ->
                entities.add(mapperFunc(feedItem))
            }
            return entities.toList()
        }
    }
}
