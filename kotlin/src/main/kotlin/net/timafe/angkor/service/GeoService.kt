package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import net.timafe.angkor.domain.dto.Coordinates
import net.timafe.angkor.domain.dto.GeoPoint
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import javax.annotation.PostConstruct


/**
 * GeoService for reverse lookup of coordinates to places
 *
 * Current implementation uses https://nominatim.openstreetmap.org service
 * whose terms and conditions mandate that we should not exceed 1 request per minute
 * to avoid issues during scheduled bulk tasks (e.g. import large amounts for Blog Posts)
 * We experiment with the https://github.com/vladimir-bukhtoyarov/bucket4j API for rate limiting
 * See also https://www.baeldung.com/spring-bucket4j
 */
@Service
class GeoService(
    @Value("\${app.osm-api-base-url}") // "https://nominatim.openstreetmap.org"
    private val osmApiService: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var bucket: Bucket
    private val requestsPerSecond = 1L

    // Todo consider dedicated rate limit exception
    // internal class AccountResourceException(message: String) : RuntimeException(message)

    /**
     * Init bucket with a limit designed for max usage of 1 request per second
     * See example usage:
     * https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/doc-pages/basic-usage.md
     */
    @PostConstruct
    fun initBucket4j() {
        val limit = Bandwidth.simple(requestsPerSecond, Duration.ofSeconds(1))
        bucket = Bucket4j.builder().addLimit(limit).build()
    }

    /**
     * Rate Limit save lookup
     *
     * @throws IllegalStateException if rate limit is exceeded
     */
    fun reverseLookupWithRateLimit(coordinates: Coordinates): GeoPoint {
        if (bucket.tryConsume(1)) {
            return reverseLookup(coordinates)
        } else {
            val msg =("ExternalService Rate Limit of $requestsPerSecond per second is exhausted")
            log.warn(msg)
            throw IllegalStateException(msg)
        }
    }

    /**
     * Lookup a place by coordinates
     *
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
