package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Post
import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.PostRepository
import net.timafe.angkor.service.interfaces.Importer
import net.timafe.angkor.service.utils.TaggingUtils
import org.jdom2.Content
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.isDirectory

@Service
class PostService(
    private val repo: PostRepository,
    @Value("\${app.tours.import-folder}") // /tmp/upload, formerly "\${user.home}/.angkor/import"
    private val importFolder: String,
    geoService: GeoService, // just pass to superclass
    private val userService: UserService,
): Importer, AbstractLocationService<Post, Post, UUID>(repo, geoService)   {

    override fun entityType(): EntityType = EntityType.Post

    /**
     * Import blog Posts from WordPress *.xml exports (extended RSS) in import folder
     */
    @Scheduled(fixedRateString = "43200", initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun importAsync() {
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
        val result = import()
        log.info("${logPrefix()} Finished scheduled import, result=$result")
    }

    override fun import(): BulkResult {
        val bulkResult = BulkResult()
        val importPath = Paths.get(importFolder)
        if (!importPath.isDirectory()) {
            log.warn("${logPrefix()} ImportFolder $importFolder does not exist (or is not a directory)")
            return bulkResult
        }
        Files.walk(importPath)
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".xml") }
            .forEach { importXML(it) }
        log.info("${logPrefix()} Finished checking $importFolder from potential files")
        return bulkResult // todo fill with values
    }

    /**
     * Import content from a specific XML file
     */
    fun importXML(item: Path) {
        log.debug("${logPrefix()} Start importing: $item") // item is full path
        val input = SyndFeedInput()
        val feed: SyndFeed = input.build(XmlReader(item.toFile()))
        val blogPosts = mutableListOf<Post>()

        // Convert all applicable RSS Items to Posts
        feed.entries
            // Todo: check foreign markup list
            // media is also considered a feed entry. Better: Match <wp:post_type>attachment</wp:post_type or feedback vs post
            //.filter { entry -> ! (entry.uri.lowercase().endsWith(".jpg") || entry.uri.lowercase().endsWith(".png")) }
            .filter { entry -> isPost(entry) }
            .forEach { entry -> blogPosts.add(mapPost(entry)) }

        var (inserted,exists) = listOf(0,0)
        for (importPost in blogPosts) {
            val existPost = repo.findOneByExternalId(importPost.externalId!!)
            // No hit in our DB -> New Post
            if (existPost.isEmpty) {
                log.info("${logPrefix()} Saving new imported post ${importPost.name}")
                this.save(importPost)
                inserted++
            // Post exists, update on changes of important fields
            } else {
                val updatePost = existPost.get()
                // Update tags, This will implicitly update the existing entity,
                // no call to save() required (and hibernate is smart enough too only update if there is a change)
                TaggingUtils.mergeAndSort(updatePost,importPost.tags)
                log.trace("${logPrefix()}  ${importPost.name} already stored")
                exists++
            }
        }
        log.info("${logPrefix()} Finished parsing $item $inserted files inserted, $exists existed already")
    }

    /**
     * Map Rome SyndEntry RSS entry representation
     * to our domain object
     */
    private fun mapPost(syndEntry: SyndEntry): Post {
        val post = Post()
        post.apply {
            externalId = syndEntry.uri
            name = syndEntry.title
            primaryUrl = syndEntry.link
            //  thumbnail = extractThumbnail(entry)?.toString(),
            // description = entry.description?.value ?: "no description",
            coordinates = extractCoordinatesWP(syndEntry)
            // todo checkout ContentModule https://rometools.github.io/rome/Modules/Content.html
        } // description is of type SyndContent
        // Convert RSS Categories to tags
        // <category domain="post_tag" nicename="lord-murugan"><![CDATA[Lord Murugan]]></category>
        // <category domain="category" nicename="malaysia"><![CDATA[Malaysia]]></category>
        if (syndEntry.categories.isNotEmpty()) {
            val tags = syndEntry.categories.map { it.name }
            TaggingUtils.mergeAndSort(post,tags)
        }
        return post
    }

    /**
     * Checks if the [SyndEntry] is of type post
     * (WordPress also exports images etc. as items)
     */
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
}
