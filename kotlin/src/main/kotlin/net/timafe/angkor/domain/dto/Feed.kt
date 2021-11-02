package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * JSON Representation of RSS Feed
 * https://diamantidis.github.io/2019/10/13/json-feed-reader-app-with-kotlin-native
 * Sample JSONFeed: https://diamantidis.github.io/feed.json
 * Media images ?? https://stackoverflow.com/questions/37721580/cant-read-image-url-from-rss-using-rome-api
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Feed (
    val title: String?,
    // @SerialName("home_page_url")
    val homePageURL: String,
    // @SerialName("feed_url")
    var feedURL: String,
    val description: String,
    val icon: String? = null,
    val favicon: String? = null,
    var expired: Boolean? = null,
    val author: String, // Author
    val items: List<FeedItem>
)
