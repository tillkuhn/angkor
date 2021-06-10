package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.dto.ExternalTour
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Rest Bridge to external provider for Tour Information
 */
@Service
class TourService(private val appProperties: AppProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun loadExternal(id: Int): ExternalTour {
        val url = "${appProperties.tourApiBaseUrl}/tours/${id}"
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/hal+json")
            // .queryString("apiKey", "123")
            .asJson()
        log.info("Downloading tour info for $id from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(HttpStatus.valueOf(jsonResponse.status), "Could not retrieve tour info from $url")
        }
        val bodyObject = jsonResponse.body.`object`
        val startPoint = bodyObject.getJSONObject("start_point")
        val coordinates: List<Double> = listOf(startPoint.getDouble("lng"),startPoint.getDouble("lat"))
        // nice2have: We could  also add lat which is an integer
        return ExternalTour(externalId=bodyObject.getInt("id"),
            name=bodyObject.getString("name"),
            altitude = startPoint.getInt("alt"),
            coordinates = coordinates)
    }
}
