package net.timafe.angkor.domain.dto

/**
 * JSON Representation of RSS Feed
 * based on https://diamantidis.github.io/2019/10/13/json-feed-reader-app-with-kotlin-native
 * https://diamantidis.github.io/2019/10/13/json-feed-reader-app-with-kotlin-native
 * Sample JSONFeed: https://diamantidis.github.io/feed.json
 */
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
