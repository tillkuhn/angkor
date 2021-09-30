package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.ExternalTour
import net.timafe.angkor.repo.TourRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

/**
 * Rest Bridge to external provider for Tour Information
 */
@Service
class TourService(
    private val appProperties: AppProperties,
    private val tourRepository: TourRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun loadSingleExternalTour(userId: Int): ExternalTour {
        val url = "${appProperties.tourApiBaseUrl}/tours/${userId}" // api ends with bond
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            // .queryString("apiKey", "123")
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
    @Scheduled(fixedRateString = "86400000", initialDelay = 15000)
    @Transactional
    fun loadExternalTourList(): List<ExternalTour> {
        val tours = mutableListOf<ExternalTour>()
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
            val extTour = mapExternalTour(it as JSONObject)
            val externalId = extTour.externalId.toString()
            val tour = Tour(tourUrl = "https://www.komoot.de/tour/${externalId}")
            tour.externalId = externalId
            tour.name = extTour.name
            tour.coordinates = extTour.coordinates
            val existTour = tourRepository.findOneByExternalId(externalId)
            if (existTour == null) {
                log.info("Saving new tour ${tour.name}")
                tourRepository.save(tour)
                inserted++
            } else {
                log.trace("Tour ${tour.name} already stored")
                exists++
            }
            tours.add(extTour)
        }
        log.info("Finished scanning ${inserted+exists} tours, $inserted inserted, $exists were already stored")
        return tours
    }

    private fun mapExternalTour(jsonObject: JSONObject): ExternalTour {
        val startPoint = jsonObject.getJSONObject("start_point")
        val coordinates: List<Double> = listOf(startPoint.getDouble("lng"), startPoint.getDouble("lat"))
        // nice2have: We could  also add lat which is an integer
        return ExternalTour(
            externalId = jsonObject.getInt("id"),
            name = jsonObject.getString("name"),
            altitude = startPoint.getInt("alt"),
            coordinates = coordinates
            // "status": "public",
            // "date": "2021-09-26T14:51:34.586+02:00",
            // "sport": "hike",
            //  "id": 499994101,
            // "map_image_preview": {
            //      "src": "https://photos ...
        )

    }
}
