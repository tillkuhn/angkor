package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.domain.dto.Coordinates
import net.timafe.angkor.domain.dto.GeoPoint
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class GeoService(
    @Value("\${app.osm-api-base-url}")
    private val osmApiService: String = "https://nominatim.openstreetmap.org"
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Lookup a place by coordinates
     *
     * TODO Rate Limiting to 1 request per second, see
     * https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/doc-pages/basic-usage.md
     *
     * curl -i 'https://nominatim.openstreetmap.org/reverse?lat=13.7435571&lon=100.4898632&format=jsonv2'
     */
    fun reverseLookup(coordinates: Coordinates): GeoPoint {
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get("$osmApiService/reverse")
            .header("accept", "application/json")
            .header("User-Agent", this.javaClass.simpleName)
            .queryString("lat", coordinates.lat)
            .queryString("lon", coordinates.lon)
            .queryString("format", "jsonv2")
            .asJson()
        log.info("Downloading geo info for $coordinates from $osmApiService status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve geo data from $osmApiService HTTP Status ${jsonResponse.status}",
            )
        }
        val jsonObject = jsonResponse.body.`object`
        log.info("Json: $jsonObject")
        // return mapExternalTour(jsonObject)
        return mapToOSMPlaceSummary(jsonObject)
    }

    private fun mapToOSMPlaceSummary(json: JSONObject): GeoPoint {
        //  JSONObject["name"] not a string. exception if entity is null

        return GeoPoint(
            countryCode = json.getJSONObject("address").getString("country_code"),
            lat = json.getString("lat").toDouble(),
            lon = json.getString("lat").toDouble(),
            osmId = json.getLong("osm_id"),
            name =  json.getString("display_name"), // todo check name first
            type = json.getString("type"),
        )
    }

}
