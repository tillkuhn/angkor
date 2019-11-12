package org.timafe.p2b

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.timafe.p2b.model.Place
import org.timafe.p2b.model.Response

@RestController
@RequestMapping("/api/v1/places")
class PlaceController {

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun place(): List<Place> {
        val places =  placeRepository.findAll()
        log.info("return {0} places",places.size)
        return places
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun singleplace(@PathVariable id: String): Place {
        log.info("looking for id $id")
        return placeRepository.findById(id).get();
    }


    @PostMapping(consumes = ["application/json"],
            produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrUpdatePlace(@RequestBody place: Place): Response {
        log.info(if (place.id != null) "update () called for place ${place}" else "new place ${place}")
        if ( place.id != null &&  placeRepository.existsById(place.id) ) {
            val ePlace = placeRepository.findById(place.id).get()
            ePlace.name = place.name
            ePlace.desc = place.desc
            placeRepository.save(ePlace);
            return Response(result = "Updated ${ePlace.id}")
        }   else {
            placeRepository.save(place);
            return Response(result = "Created ${place.id}")
        }

    }

}
