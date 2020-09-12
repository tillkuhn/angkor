package net.timafe.angkor.rest

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.service.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*
import javax.persistence.EntityManager
import javax.validation.Valid

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/places")
class PlaceController {

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get public places if logged in and all places if not ...
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun allPlaces(principal: Principal?): List<PlaceSummary> {
        val isAnonymous = authService.isAnonymous()
        val places = if (isAnonymous) placeRepository.findPublicPlaces() else placeRepository.findAllPlacesOrderByName()
        //  coo ${places.get(0).coordinates}"
        log.info("allPlaces() returns ${places.size} happy places anoymous=$isAnonymous")
        return places
    }

    /**
     * Get all details of a single place
     */
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun singleplace(@PathVariable id: UUID): ResponseEntity<Place> {
        return placeRepository.findById(id).map { place ->
            ResponseEntity.ok(place)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Post a new place
     */
    //@RequestMapping(method = [RequestMethod.POST,RequestMethod.PUT])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewPlace(@RequestBody place: Place): Place = placeRepository.save(place)


    /**
     * Updates a place, this operation neefs to be adapted if we add new attributes
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    fun updatePlace(@Valid @RequestBody newPlace: Place, @PathVariable id: UUID): ResponseEntity<Place> {
        log.info("update () called for place $id")
        return placeRepository.findById(id).map { existingPlace ->
            val updatedPlace: Place = existingPlace
                    .copy(name = newPlace.name,
                            summary = newPlace.summary,
                            notes = newPlace.notes,
                            locationType = newPlace.locationType,
                            areaCode = newPlace.areaCode,
                            primaryUrl = newPlace.primaryUrl,
                            imageUrl = newPlace.imageUrl,
                            coordinates = newPlace.coordinates,
                            authScope = newPlace.authScope
                    )
            ResponseEntity.ok().body(placeRepository.save(updatedPlace))
        }.orElse(ResponseEntity.notFound().build())
    }


    // https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
    @DeleteMapping("{id}")
    fun deletePlaceById(@PathVariable(value = "id") placeId: UUID): ResponseEntity<Void> {
        log.debug("Deleting place $placeId")
        return placeRepository.findById(placeId).map { place ->
            placeRepository.delete(place)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())

    }

}
