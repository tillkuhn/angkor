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
import org.springframework.security.core.context.SecurityContextHolder
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
    private val areaService: AreaService,
    private val userService: UserService,
): Importer, AbstractLocationService<Photo, Photo, UUID>(repo, geoService)   {

    override fun entityType(): EntityType = EntityType.Photo

    /**
     * Import Photos from RSS Feed.
     *
     * CAUTION: If delay is too low, it may conflict with integration test
     * Better disabled scheduled during tests completely
     */
    @Scheduled(fixedRateString = "43200", initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun import() {
        // @Scheduled runs without Auth Context, so we use a special ServiceAccountToken here
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)

        this.log.info("${this.logPrefix()} Checking for recent photos to import from RSS $feedUrl")
        val photos = FeedUtils.parseFeed(feedUrl,::mapFeedItemToEntity)
        var (inserted,exists) = listOf(0,0)

        val countries = areaService.countriesAndRegions().filter { it.emoji?.isNotEmpty() == true }
        for (feedPhoto in photos) {

            // check if we can derive countryCode from emoji in photo name
            for (country in countries) {
                if (feedPhoto.name.contains(country.emoji!!)) { // we asserted not empty some lines above
                    feedPhoto.areaCode = country.code
                    feedPhoto.coordinates = country.coordinates
                    break
                }
            }
            val existPhoto = repo.findOneByExternalId(feedPhoto.externalId!!)
            // No hit in our DB -> New Post
            if (existPhoto.isEmpty) {
                log.info("${logPrefix()} Saving new imported photo ${feedPhoto.name}")
                this.save(feedPhoto)
                inserted++
                // Photo exists, update on changes of important fields
            } else {
                val updatePhoto = existPhoto.get()
                // Update tags, This will implicitly update the existing entity,
                // no call to save() required (and hibernate is smart enough too only update if there is a change)
                updatePhoto.name = feedPhoto.name
                // if updated photo already has area code, keep it - else take the one from the feed
                updatePhoto.areaCode = updatePhoto.areaCode?: feedPhoto.areaCode
                // if update photo has LonLat keep them, else use feedPhoto default for country
                updatePhoto.coordinates = if (updatePhoto.hasCoordinates()) updatePhoto.coordinates else feedPhoto.coordinates

                TaggingUtils.mergeAndSort(updatePhoto,feedPhoto.tags)
                log.trace("${logPrefix()} ${updatePhoto.name} already stored")
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
            // we need to strip the suffix as it changes when we change the name  whereas the /photo/1038580812
            // part is stable, example https://999px.com/photo/1038580812/Salines-di-Marsala--by-hase/
            externalId = syndEntry.uri.substringBeforeLast("/")
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
