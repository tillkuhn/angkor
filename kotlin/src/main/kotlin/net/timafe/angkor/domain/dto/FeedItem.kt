package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.domain.interfaces.Mappable
import java.time.ZonedDateTime

/**
 * Loosely based on https://diamantidis.github.io/2019/10/13/json-feed-reader-app-with-kotlin-native
 */
data class FeedItem (
    val id: String,
    val url: String,
    val title: String,
    val thumbnail: String?,
    // @SerialName("date_published")
    val datePublished: ZonedDateTime? = null,
    // @SerialName("date_modified")
    val dateModified:  ZonedDateTime? = null,
    val author: String? = null, // Author
    val description: String? = null,
   // @SerialName("content_html")
    val contentHtml: String? = null,
    override var coordinates: List<Double> = listOf() /* LON, LAT */,
): Mappable
