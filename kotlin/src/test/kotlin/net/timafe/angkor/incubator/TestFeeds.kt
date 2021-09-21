package net.timafe.angkor.incubator

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import net.timafe.angkor.domain.dto.FeedItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * Check out
 * https://github.com/rometools/rome
 * https://jsonfeed.org/mappingrssandatom
 *
 */
class TestFeeds {

    @Test
    fun rss() {
        //
        val input = SyndFeedInput()
        //val feed: SyndFeed = input.build(XmlReader( URL("https://www.feedforall.com/sample.xml")))
        val feed: SyndFeed = input.build(javaClass.getResourceAsStream("/test-feed.xml").bufferedReader()) //.readLines()
        assertNotNull(feed)
        assertThat(feed.entries.size).isGreaterThan(0)
        val items = mutableListOf<FeedItem>()
        feed.entries.forEach { entry ->
            items.add(FeedItem(id = entry.uri, title = entry.title, url = entry.link, thumbnail = null))
        }
        val jsonFeed = net.timafe.angkor.domain.dto.Feed(
            title = feed.title,
            author = "hase", description = feed.description, feedURL = "url", homePageURL = "", items = items
        )
        assertThat(jsonFeed.items.size).isGreaterThan(0)
        assertThat(jsonFeed.title).isEqualTo("UnitTesting on Tour")
        assertThat(jsonFeed.items[0].title).isEqualTo("How to unit-test RSS")
        // feed.entries.forEach {println(it.title)}
    }

}
