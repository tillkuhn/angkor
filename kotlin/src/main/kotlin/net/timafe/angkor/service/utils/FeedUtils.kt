package net.timafe.angkor.service.utils

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.ParsingFeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.IOException

class FeedUtils {

    companion object {

        val log: Logger = LoggerFactory.getLogger(FeedUtils::class.java)

        fun <T> parseFeed(feedUrl: String, mapperFunc: (syndEntry: SyndEntry) -> T): List<T> {
            val input = SyndFeedInput()
            log.info("[Feeder] Parsing feedUrl $feedUrl")
            // external feed could return invalid XML, in which case we log the error but return empty list
            return try {
                val feed: SyndFeed? = if (feedUrl.startsWith("https://")) getSyndFeedFromURL(feedUrl)
                else input.build(XmlReader(File(feedUrl))) //.readLines()
                val entities = mutableListOf<T>()
                if (feed == null) {
                    log.error("[Feeder] Feed is null for $feedUrl")
                } else {
                    feed.entries.forEach { feedItem ->
                        entities.add(mapperFunc(feedItem))
                    }
                }
                entities.toList()
            } catch (pfe: ParsingFeedException) {
                log.error("[Feeder] Parse Error, cannot build RSS feed from ${feedUrl}: ${pfe.message}}")
                emptyList()
            }
        }

        /**
         * input.build(XmlReader(URL(feedUrl))) for XmlReader is deprecated, the workaround
         * is not that straightforward, but at least prevents the warning.
         * see https://github.com/rometools/rome/issues/276
         */
        fun getSyndFeedFromURL(feedUrl: String): SyndFeed? {
            val restTemplate = RestTemplate()
            log.info("[Feeder] Loading feedUrl from $feedUrl")
            return restTemplate.execute(feedUrl, HttpMethod.GET, null,
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
        }
    }

}

