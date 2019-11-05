package org.timafe.p2b

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

@RestController
class GreetingController {

    @Autowired
    private lateinit var placeRepository: PlaceRepository


    val counter = AtomicLong()
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name")

    @GetMapping("/places")
    @ResponseStatus(HttpStatus.OK)
    fun place() : List<Place> {
        log.info("HUHUHUHU all")
        // UUID.randomUUID().toString()
        // Random.nextInt(0, 100)
        //var place = Place(name = "Horstingen",id=null)

        //place.idid= null)
        //placeRepository.save(place)
        return placeRepository.findAll()
        //return emptyList()
    }

    @GetMapping("/place")
    @ResponseStatus(HttpStatus.OK)
    fun singleplace() : Place {
        log.info("looking for")

        return placeRepository.findByName("Horstingen").get(0)

    }


}
