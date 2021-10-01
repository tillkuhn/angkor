package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.ExternalTour
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.TourRepository
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

/**
 * Rest Bridge to external provider for Tour Information
 */
@Service
class TourService(
    private val appProperties: AppProperties,
    private val tourRepository: TourRepository,
    private val taggingService: TaggingService,
): AbstractEntityService<Tour, Tour, UUID>(tourRepository)  {

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

    // 600000 = 10 min, 3600000 = 1h, 86400000 = 1day
    @Scheduled(fixedRateString = "86400000", initialDelay = 5000)
    @Transactional
    fun loadTourList(): List<Tour> {
        val tours = mutableListOf<Tour>()
        val userId = appProperties.tourApiUserId
        val url = "${appProperties.tourApiBaseUrl}/users/${userId}/tours/"
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            .queryString("type","tour_recorded")
            .queryString("sort_field","date")
            .queryString("sort_direction","desc")
            .queryString("status","public")
            .asJson()

        log.info("Downloading tour list for $userId from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve tour list info from $url: Status ${jsonResponse.status}"
            )
        }

        val results = jsonResponse.body.`object`.getJSONObject("_embedded").getJSONArray("tours")
        var (inserted,exists) = listOf(0,0)
        results.iterator().forEach {
            val tour = mapTour(it as JSONObject)

            val existTour = tourRepository.findOneByExternalId(tour.externalId!!)
            if (existTour == null) {
                log.info("Saving new tour ${tour.name}")
                this.save(tour)
                inserted++
            } else {
                log.trace("Tour ${tour.name} already stored")
                exists++
            }
            tours.add(tour)
        }
        log.info("Finished scanning ${inserted+exists} tours, $inserted inserted, $exists were already stored")
        return tours
    }

    // This is the new way ...
    private fun mapTour(jsonTour: JSONObject): Tour {
        val startPoint = jsonTour.getJSONObject("start_point")
        val coordinates: List<Double> = listOf(startPoint.getDouble("lng"), startPoint.getDouble("lat"))
        // nice2have: We could  also add lat which is an integer
        val externalId = jsonTour.getInt("id")
        val tour = Tour(tourUrl = "https://www.komoot.de/tour/${externalId}")
        tour.externalId = externalId.toString()
        tour.name = jsonTour.getString("name")
        // tour.altitude = startPoint.getInt("alt"),
        tour.coordinates = coordinates
        tour.properties["alt"] = startPoint.getInt("alt").toString()
        tour.tags.add(jsonTour.getString("sport"))
        tour.imageUrl = extractImage(jsonTour)
        // "status": "public",
        // "date": "2021-09-26T14:51:34.586+02:00",
        // "sport": "hike",
        //  "id": 499994101,
        // "map_image_preview": {
        //      "src": "https://photos ...
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
        return image.substring(0,image.indexOf("?") -1)
    }

    override fun entityType(): EntityType = EntityType.TOUR

}
