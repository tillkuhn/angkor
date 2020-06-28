package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.repo.GeocodeRepository
import net.timafe.angkor.domain.Geocode
import net.timafe.angkor.domain.GeocodeLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION )
class GeocodeController {

    @Autowired
    private lateinit var geocodeRepository: GeocodeRepository

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping( "/geocodes")
    fun geocodes(): List<Geocode> {
        return geocodeRepository.findAll()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping( "/countries")
    fun countries(): List<Geocode> {
        return geocodeRepository.findByLevel(GeocodeLevel.COUNTRY)
    }


}
