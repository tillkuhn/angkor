package net.timafe.angkor.service

import com.rometools.rome.feed.synd.SyndEntry
import net.timafe.angkor.domain.Photo
import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.domain.dto.ExternalPhoto
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
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.io.path.isDirectory

@Service
class PhotoService(
    private val repo: PhotoRepository,
    @Value($$"${app.photos.feed-url}")  private val feedUrl: String,
    @Value($$"${app.photos.import-folder}") private val importFolder: String,
    geoService: GeoService, // just pass to superclass
    private val areaService: AreaService,
    private val userService: UserService,
    private val jsonMapper: JsonMapper,
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
    override fun importAsync() {
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
        import()
    }

    override fun import(): BulkResult {
        val bulkResult = BulkResult()
        if (this.feedUrl.isEmpty()) {
            log.info("${this.logPrefix()} feedUrl is empty, nothing to import")
            return bulkResult
        }
        log.info("${this.logPrefix()} Checking for recent photos to import from RSS $feedUrl")

        val photos = FeedUtils.parseFeed(feedUrl,::mapFeedItemToEntity)
        val countries = areaService.countriesAndRegions().filter { it.emoji?.isNotEmpty() == true }

        for (feedPhoto in photos) {
            bulkResult.read++
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
                log.info("${logPrefix()} Inserting new imported photo ${feedPhoto.name}")
                // WARNING: @Transactional self-invocation (in effect, a method within the target object calling another method
                // of the target object) does not lead to an actual transaction at runtime
                this.save(feedPhoto)
                bulkResult.inserted++
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
                bulkResult.updated++
            }
        }
        log.info("${logPrefix()} Finished importing $feedUrl: $bulkResult")
        return bulkResult
    }

    /**
     * Map Rome SyndEntry RSS entry representation
     * to our Photo domain object, can be passed as a function to FeedUtils
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

    /**
     * Scan the folder for files in json format
     */
    fun importFromFolder(): BulkResult {
        val totals = BulkResult()
        val importPath = Paths.get(importFolder)
        if (!importPath.isDirectory()) {
            log.warn("${logPrefix()} ImportFolder $importFolder does not exist (or is not a directory)")
            return totals
        }
        Files.walk(importPath)
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().contains("photo") }
            .filter { it.toString().endsWith(".json") }
            .forEach { totals.add(importFromFile(it)) }
        log.info("${logPrefix()} Finished checking $importFolder from potential files")
        return totals
    }

    /**
     * Import content of a particular JSON file, usually called from local folder import
     */
    private fun importFromFile(inputFile: Path): BulkResult {
        // https://www.baeldung.com/jackson-deserialize-json-unknown-properties#2-dealing-with-unknown-fields-using-the-objectmapper
        //val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val bulkResult = BulkResult()
        this.log.info("${logPrefix()} Import from $inputFile")
        val jsonNode = jsonMapper.readTree(inputFile.toFile())
        val jsonPhotos = jsonNode.get("photos")
        if (jsonPhotos.isArray) {
            // now we're talking
            for (jsonPhoto in jsonPhotos) {
                val extPhoto = jsonMapper.treeToValue(jsonPhoto,ExternalPhoto::class.java)
                log.trace("ExtPhoto: {}", extPhoto)
                val photo = mapExternalPhotoToEntity(extPhoto)
                log.debug("OurPhoto: {}", photo)
                bulkResult.read += 1
            }
        }
        return bulkResult
    }

    /** Map external Photo format to our domain entity */
    private fun mapExternalPhotoToEntity(extPhoto: ExternalPhoto): Photo {
        val photo = Photo()
        photo.apply {
            // we need to strip the suffix as it changes when we change the name  whereas the /photo/1038580812
            // part is stable, example https://999px.com/photo/1038580812/Salines-di-Marsala--by-hase/
            externalId = extPhoto.id.toString() // syndEntry.uri.substringBeforeLast("/")
            name = extPhoto.name // syndEntry.title
            primaryUrl = extPhoto.url // syndEntry.link
            imageUrl = if (extPhoto.imageUrls.size >= 2) extPhoto.imageUrls[extPhoto.imageUrls.lastIndex -2] else extPhoto.imageUrls.last()
            tags = extPhoto.tags
        }
        if (extPhoto.longitude != null && extPhoto.latitude != null) {
            photo.coordinates = listOf(extPhoto.longitude, extPhoto.latitude)
        }
        if (extPhoto.focalLength != null) {
            photo.properties["focalLength"] = extPhoto.focalLength
        }
        if (extPhoto.shutterSpeed != null) {
            photo.properties["shutterSpeed"] = extPhoto.shutterSpeed
        }
        return photo
    }
}
