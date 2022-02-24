package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.repo.TourRepository
import net.timafe.angkor.service.interfaces.Importer
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Rest Bridge to external provider for Tour Information
 */
@Service
class TourService(
    private val appProperties: AppProperties,
    private val repo: TourRepository,
    private val userService: UserService,
    private val eventService: EventService,
    geoService: GeoService,
) : Importer, AbstractLocationService<Tour, Tour, UUID>(repo, geoService) {

    override fun entityType(): EntityType = EntityType.Tour

    /**
     * Import tours from REST Call
     */
    // 600 = 10 min, 3600 = 1h, 86400 = 1day (using seconds, 43200 = 12h default is millis)
    @Scheduled(fixedRateString = "43200", initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun importAsync() {
        // @Scheduled runs without Auth Context, so we use a special ServiceAccountToken here
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)
        import()
    }

    override fun import(): BulkResult {
        val baseResult = importTours("planned")
        val recordedResult = importTours("recorded")
        baseResult.add(recordedResult)
        return baseResult
    }


    fun importTours(tourType: String): BulkResult {
        val bulkResult = BulkResult()
        val tours = mutableListOf<Tour>()
        val userId = appProperties.tours.apiUserId
        val url = "${appProperties.tours.apiBaseUrl}/users/${userId}/tours/"
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            .queryString("type", "tour_${tourType}") // or tour_planned
            .queryString("sort_field", "date")
            .queryString("sort_direction", "desc")
            .queryString("status", "public")
            .asJson()

        log.info("${logPrefix()} Downloading $tourType tour list for $userId from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve tour list info from $url: Status ${jsonResponse.status}"
            )
        }

        val results = jsonResponse.body.`object`.getJSONObject("_embedded").getJSONArray("tours")
        results.iterator().forEach {
            bulkResult.read++
            val tourToImport = mapTour(it as JSONObject)
            val singleBulkResult = createOrUpdateTour(tourToImport)
            bulkResult.add(singleBulkResult) // merge figures from single resul into our master result
            tours.add(tourToImport)
        }
        log.info("${logPrefix()} Finished scanning $tourType tours, result=$bulkResult")
        return bulkResult
    }

    /**
     * takes a transient Tour as argument, checks if it exists in the DB,
     * and either creates a new entity or updates an existing one
     */
    private fun createOrUpdateTour(tourToImport: Tour): BulkResult {
        val singleBulkResult = BulkResult()
        val tourType = tourToImport.properties["type"]
        val existTour = repo.findOneByExternalId(tourToImport.externalId!!)
        if (existTour.isEmpty) {
            log.info("${logPrefix()} Importing new tour ${tourToImport.name}")
            this.save(tourToImport)
            singleBulkResult.inserted++
            // TODO support EntityEventListener in Tour
            val em = Event(
                entityId = tourToImport.id,
                action = "import:${EntityType.Tour.name.lowercase()}",
                message = "Tour '${tourToImport.name}' ($tourType) successfully imported",
                source = this.javaClass.simpleName // e.g. TourService
            )
            eventService.publish(EventTopic.APP, em)
        } else {
            log.trace("$tourType tour ${tourToImport.name} already stored")
            // Update selected fields
            val tour = existTour.get()
            if (tour.name != tourToImport.name) {
                log.debug("${logPrefix()} $tour name changed to ${tourToImport.name}")
                tour.name = tourToImport.name
                // Comment: creates a stack overflow on current user - is this still valid?
                // TODO support EntityEventListener in Tour
                val em = Event(
                    entityId = tourToImport.id,
                    action = "update:${EntityType.Tour.name.lowercase()}",
                    message = "Tour '${tourToImport.name}' ($tourType) successfully updated",
                    source = this.javaClass.simpleName
                )
                eventService.publish(EventTopic.APP, em)
                singleBulkResult.updated++
            }
        }
        return singleBulkResult
    }

    /**
     * This is the new way to map from external json to "our" Tour entity
     */
    private fun mapTour(jsonTour: JSONObject): Tour {
        val startPoint = jsonTour.getJSONObject("start_point")
        val theirId = jsonTour.getInt("id") // e.g. 111999111 but internally it's a string
        val status = jsonTour.getString("status") // public or private
        val newAuthScope = if ("public" == status) AuthScope.PUBLIC else AuthScope.RESTRICTED
        val tour = Tour(tourUrl = "https://www.komoot.de/tour/${theirId}")
        tour.apply {
            externalId = theirId.toString()
            name = jsonTour.getString("name")
            coordinates = listOf(startPoint.getDouble("lng"), startPoint.getDouble("lat"))
            properties["alt"] = startPoint.getInt("alt").toString()
            properties["type"] = jsonTour.getString("type").toString()
            tags.add(jsonTour.getString("sport")) // e.g. hike
            imageUrl = extractImage(jsonTour)
            authScope = newAuthScope
            beenThere = extractDate(jsonTour.getString("date"))
        }
        return tour
    }

    private fun extractImage(jsonTour: JSONObject): String {
        // https://photos.komoot.de/www/maps/493432445-4d35762ca8de4af3dc9f4cb2a9cd47e5875b72899dd8bf49f6ea69f4cf89c44c-small@2x.jpg/17c050f2712
        // map_image_preview and map_image, both have a "src" attribute
        val image = jsonTour.getJSONObject("map_image_preview").getString("src")
        return image.substring(0, image.indexOf("?")) // excludes the '?'
    }

    private fun extractDate(jsonDate: String): LocalDate? {
        // '2021-09-26T14:51:34.586+02:00'
        // could not be parsed, unparsed text found at index 10
        val datePart = jsonDate.substring(0, jsonDate.indexOf("T"))
        var localDate: LocalDate? = null
        try {
            localDate = LocalDate.parse(datePart)
        } catch (e: DateTimeParseException) {
            log.warn("Cannot convert $jsonDate to LocalDate: ${e.message}")
        }
        return localDate
    }

    /**
     * Convenient function to load a single tour by id (which will be transformed into alink
     * and delegated to loadSingleTour(ir: importRequest)
     */
    fun importExternal(tourId: Int): Tour {
        return importExternal(
            ImportRequest(
                importUrl = "${appProperties.tours.apiBaseUrl}/tours/${tourId}",
                targetEntityType = EntityType.Tour
            )
        )
    }

    /**
     * Load tour based on import request, usually containing a shared link
     */
    fun importExternal(importRequest: ImportRequest): Tour {
        val url = transformSharedLinkUrl(importRequest.importUrl) // api ends with bond
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            .asJson()
        // extract tour id later
        log.info("Downloading tour info for ${importRequest.importUrl} from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve tour info from $url"
            )
        }
        val jsonObject = jsonResponse.body.`object`
        val mappedTour =  mapTour(jsonObject)
        // overwrite default generated URL with the one from the request, as it may contain shared token etc.
        mappedTour.primaryUrl = importRequest.importUrl
        createOrUpdateTour(mappedTour)
        return mappedTour
    }

    /**
     * Transform shared link URLs (which return HTML) to api calls (which return JSON)
     * Source Example: https://www.tumuult.de/tour/111999111?share_token=abc&ref=wtd
     * Target Example: https://api.tumuult.de/v123/tours/111999111?share_token=abc&ref=wtd

     */
    private fun transformSharedLinkUrl(url: String): String {
        // if url already starts with api base url, there's nothing to transform
        return if (url.startsWith(appProperties.tours.apiBaseUrl)) url
        else appProperties.tours.apiBaseUrl + "/tours/" + url.substringAfterLast("/")
    }

    /**
     * Experiment: Generate a predictable and comparable hashcode that represents the hashcode of the key fields
     * we consider relevant for comparison
     *
     * Inspiration: Generating hashCode from multiple fields?
     * https://docs.oracle.com/javase/7/docs/api/java/util/List.html#hashCode%28%29
     * (...) recommend using the same sort of logic as is used by java.util.List.hashCode(), which is a
     * straightforward and effective way to assemble the hash-codes of component objects in a specific order;
     */
    fun keyFieldsHashCode(tour: Tour): Int {
        var hashCode = 1
        hashCode = 31 * hashCode + (tour.geoAddress?.hashCode() ?: 0)
        hashCode = 31 * hashCode + tour.name.hashCode()
        hashCode = 31 * hashCode + (tour.externalId?.hashCode() ?:0)
        hashCode = 31 * hashCode + tour.properties.hashCode()
        return hashCode

    }

}
