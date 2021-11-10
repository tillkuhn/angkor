package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.timafe.angkor.domain.Post
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.PostRepository
import net.timafe.angkor.service.interfaces.Importer
import org.jdom2.Content
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
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
    // @Value("\${user.home}/.angkor/import")
    @Value("\${app.tours.import-folder}")
    private val importFolder: String,
    // private val taskScheduler: TaskScheduler
    geoService: GeoService,
): Importer, AbstractLocationService<Post, Post, UUID>(repo,geoService)   {

    override fun entityType(): EntityType = EntityType.POST

    @Scheduled(fixedRateString = "43200", initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun import() {
        val importPath = Paths.get(importFolder)
        if (!importPath.isDirectory()) {
            log.warn("${logPrefix()} ImportFolder $importFolder does not exist (or is not a directory)")
            return
        }
        Files.walk(importPath)
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".xml") }
            .forEach { importXML(it) }
        log.info("${logPrefix()} Finished checking $importFolder from potential files")
    }

    fun importXML(item: Path) {
        log.debug("${logPrefix()} Start importing: $item") // item is full path
        val input = SyndFeedInput()
        val feed: SyndFeed = input.build(XmlReader(item.toFile()))
        val blogPosts = mutableListOf<Post>()
        feed.entries
            // Todo: check foreign markup list
            // media is also considered a feed entry. Better: Match <wp:post_type>attachment</wp:post_type or feedback vs post
            //.filter { entry -> ! (entry.uri.lowercase().endsWith(".jpg") || entry.uri.lowercase().endsWith(".png")) }
            .filter { entry -> isPost(entry) }
            .forEach { entry -> blogPosts.add(mapPost(entry)) }

        var (inserted,exists) = listOf(0,0)
        for (post in blogPosts) {
            val existPost = repo.findOneByExternalId(post.externalId!!)
            if (existPost.isEmpty) {
                log.info("${logPrefix()} Saving new imported post ${post.name}")
                this.save(post)
                inserted++
            } else {
                log.trace("${logPrefix()}  ${post.name} already stored")
                exists++
            }
        }
        log.info("${logPrefix()} Finished parsing $item $inserted files inserted, $exists existed already")
    }

    private fun mapPost(entry: SyndEntry): Post {
        val post = Post()
        post.apply {
            externalId = entry.uri
            name = entry.title
            primaryUrl = entry.link
            //  thumbnail = extractThumbnail(entry)?.toString(),
            // description = entry.description?.value ?: "no description",
            coordinates = extractCoordinatesWP(entry)
            // todo checkout ContentModule https://rometools.github.io/rome/Modules/Content.html
        } // description is of type SyndContent
        return post
    }

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
