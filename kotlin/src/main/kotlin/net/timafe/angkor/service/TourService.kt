package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.ExternalTour
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
): Importer, AbstractLocationService<Tour, Tour, UUID>(repo, geoService) {

    override fun entityType(): EntityType = EntityType.TOUR

    fun loadSingleExternalTour(userId: Int): ExternalTour {
        val url = "${appProperties.tourApiBaseUrl}/tours/${userId}" // api ends with bond
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            .asJson()
        log.info("Downloading tour info for $userId from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve tour info from $url"
            )
        }
        val jsonObject = jsonResponse.body.`object`
        return mapExternalTour(jsonObject)
    }

    /**
     * Import tours from REST Call
     */
    // 600 = 10 min, 3600 = 1h, 86400 = 1day (using seconds, 43200 = 12h default is millis)
    @Scheduled(fixedRateString = "43200", initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    override fun import() {
        syncPlannedTours()
        syncRecordedTours()
    }

    fun syncPlannedTours(): List<Tour> = import("planned")
    fun syncRecordedTours(): List<Tour> = import("recorded")

    fun import(tourType: String): List<Tour> {
        // @Scheduled runs without Auth Context, so we use a special ServiceAccountToken here
        SecurityContextHolder.getContext().authentication = userService.getServiceAccountToken(this.javaClass)

        val tours = mutableListOf<Tour>()
        val userId = appProperties.tourApiUserId
        val url = "${appProperties.tourApiBaseUrl}/users/${userId}/tours/"
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
        var (inserted, exists) = listOf(0, 0)
        results.iterator().forEach {
            val importedTour = mapTour(it as JSONObject)
            val existTour = repo.findOneByExternalId(importedTour.externalId!!)
            if (existTour.isEmpty) {
                log.info("${logPrefix()} Saving new imported tour ${importedTour.name}")
                this.save(importedTour)
                inserted++
                // TODO support EntityEventListener in Tour
                val em = Event(
                    entityId = importedTour.id,
                    action = "import:tour",
                    message = "$tourType tour ${importedTour.name} successfully imported",
                    source = this.javaClass.simpleName
                )
                eventService.publish(EventTopic.APP, em)
            } else {
                log.trace("$tourType tour ${importedTour.name} already stored")
                // Update selected fields
                val tour = existTour.get()
                if (tour.name != importedTour.name) {
                    log.debug("${logPrefix()} $tour name changed to ${importedTour.name}")
                    tour.name = importedTour.name // TODO creates a stack overflow on current user,
                    // TODO support EntityEventListener in Tour
                    val em = Event(
                        entityId = importedTour.id,
                        action = "update:tour",
                        message = "$tourType tour ${importedTour.name} successfully updated",
                        source = this.javaClass.simpleName
                    )
                    eventService.publish(EventTopic.APP, em)
                }
                exists++
            }
            tours.add(importedTour)
        }
        log.info("${logPrefix()} Finished scanning ${inserted + exists} $tourType tours, $inserted inserted, $exists were already stored")
        return tours
    }

    // This is the new way ...
    private fun mapTour(jsonTour: JSONObject): Tour {
        val startPoint = jsonTour.getJSONObject("start_point")
        val theirId = jsonTour.getInt("id")
        val tour = Tour(tourUrl = "https://www.komoot.de/tour/${theirId}")
        tour.apply {
            externalId = theirId.toString()
            name = jsonTour.getString("name")
            coordinates = listOf(startPoint.getDouble("lng"), startPoint.getDouble("lat"))
            properties["alt"] = startPoint.getInt("alt").toString()
            tags.add(jsonTour.getString("sport")) // e.g. hike
            imageUrl = extractImage(jsonTour)
            authScope = AuthScope.PUBLIC // todo check "status": "public"
            beenThere = extractDate(jsonTour.getString("date"))
        }
        return tour
    }


    // we deprecated this soon in favor of the JPA Tour Class
    private fun mapExternalTour(jsonObject: JSONObject): ExternalTour {
        val startPoint = jsonObject.getJSONObject("start_point")
        val coordinates: List<Double> = listOf(startPoint.getDouble("lng"), startPoint.getDouble("lat"))
        // nice2have: We could  also add lat which is an integer
        return ExternalTour(
            externalId = jsonObject.getInt("id"),
            name = jsonObject.getString("name"),
            altitude = startPoint.getInt("alt"),
            coordinates = coordinates
        )
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

}
