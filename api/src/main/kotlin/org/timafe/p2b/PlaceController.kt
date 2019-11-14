package org.timafe.p2b

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.timafe.p2b.model.Place
import org.timafe.p2b.model.Response
import javax.validation.Valid

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_ROOT + "/v1/places")
class PlaceController {

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun place(): List<Place> {
        val places = placeRepository.findAll()
        log.info("return ${places.size} places")
        return places
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun singleplace(@PathVariable id: String): Place {
        log.info("looking for id $id")
        return placeRepository.findById(id).get()
    }


    //@RequestMapping(method = [RequestMethod.POST,RequestMethod.PUT])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewPlace(@RequestBody place: Place): Place = placeRepository.save(place)


    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    fun updatePlace(@Valid @RequestBody newPlace: Place, @PathVariable id: String): ResponseEntity<Place> {
        log.info("update () called for place $id")
        return placeRepository.findById(id).map { existingPlace ->
            val updatedPlace: Place = existingPlace
                    .copy(name = newPlace.name, desc = newPlace.desc)
            ResponseEntity.ok().body(placeRepository.save(updatedPlace))
        }.orElse(ResponseEntity.notFound().build())
    }


    // https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
    @DeleteMapping("{id}")
    fun deleteArticleById(@PathVariable(value = "id") placeId: String): ResponseEntity<Void> {
        log.debug("Deleting place $placeId")
        return placeRepository.findById(placeId).map { place ->
            placeRepository.delete(place)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())

    }

}
