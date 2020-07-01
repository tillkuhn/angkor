package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Geocode
import net.timafe.angkor.domain.GeocodeLevel
import net.timafe.angkor.repo.GeocodeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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
    // https://www.baeldung.com/spring-data-sorting#1-sorting-with-the-orderby-method-keyword
    fun geocodes(): List<Geocode> {
        return geocodeRepository.findByOrderByName()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping( "/countries")
    fun countries(): List<Geocode> {
        return geocodeRepository.findByLevelOrderByName(GeocodeLevel.COUNTRY)
    }


}
