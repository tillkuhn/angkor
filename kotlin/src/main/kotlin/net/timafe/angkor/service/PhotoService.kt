package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndEntry
import net.timafe.angkor.domain.Photo
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.PhotoRepository
import net.timafe.angkor.service.interfaces.Importer
import net.timafe.angkor.service.utils.FeedUtils
import net.timafe.angkor.service.utils.TaggingUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

@Service
class PhotoService(
    @Value("\${app.photos.feed-url}")  private val feedUrl: String,
    private val repo: PhotoRepository,
    geoService: GeoService, // just pass to superclass
): Importer, AbstractLocationService<Photo, Photo, UUID>(repo, geoService)   {

    override fun entityType(): EntityType = EntityType.Photo

    /**
     * Import Photos from RSS Feed.
     * CAUTION: If delay is too low, it may conflict with integration test
     * Better disabled scheduled during tests completely
     */
    @Scheduled(fixedRateString = "43200", initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun import() {
        this.log.info("${this.logPrefix()} Checking for recent photos to import from $feedUrl")
        val photos = FeedUtils.parseFeed(feedUrl,::mapFeedItemToEntity)
        var (inserted,exists) = listOf(0,0)
        for (p in photos) {
            val existPhoto = repo.findOneByExternalId(p.externalId!!)
            // No hit in our DB -> New Post
            if (existPhoto.isEmpty) {
                log.info("${logPrefix()} Saving new imported photo ${p.name}")
                this.save(p)
                inserted++
                // Photo exists, update on changes of important fields
            } else {
                val updatePhoto = existPhoto.get()
                // Update tags, This will implicitly update the existing entity,
                // no call to save() required (and hibernate is smart enough too only update if there is a change)
                TaggingUtils.mergeAndSort(updatePhoto,p.tags)
                log.trace("${logPrefix()}  ${updatePhoto.name} already stored")
                exists++
            }
        }
        log.info("${logPrefix()} Finished importing $feedUrl $inserted files inserted, $exists existed already")
    }

    /**
     * Map Rome SyndEntry RSS entry representation
     * to our domain object, can be passed as a function to FeedUtils
     */
    private fun mapFeedItemToEntity(syndEntry: SyndEntry): Photo {
        val photo = Photo()
        photo.apply {
            externalId = syndEntry.uri
            name = syndEntry.title
            primaryUrl = syndEntry.link
        }
        // extract imageUrl https://stackoverflow.com/a/33672393/4292075
        val p: Pattern = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]")
        val m: Matcher = p.matcher(syndEntry.description.value)
        if (m.find()) {
            val srcResult: String = m.group(1)
            photo.imageUrl = srcResult
        }
        return photo
    }


}
