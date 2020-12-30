package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.service.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/"+ Constants.API_PATH_PLACES)
class PlaceController(
        var repo: PlaceRepository,
        var authService: AuthService
): ResourceController<Place,PlaceSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get public places if logged in and all places if not ...
     */
    @GetMapping
    override fun getAll(): List<PlaceSummary> {
        return searchAll()
    }

    /**
     * Get all details of a single place
     */
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getItem(@PathVariable id: UUID): ResponseEntity<Place> {
        return repo.findById(id).map { place ->
            ResponseEntity.ok(place)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Post a new place
     */
    //@RequestMapping(method = [RequestMethod.POST,RequestMethod.PUT])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    override fun createItem(@RequestBody item: Place): Place = repo.save(item)

    /**
     * Updates a place, this operation needs to be adapted if we add new attributes
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun updateItem(@Valid @RequestBody newItem: Place, @PathVariable id: UUID): ResponseEntity<Place> {
        log.info("update () called for place $id")
        return repo.findById(id).map { existingPlace ->
            val updatedPlace: Place = existingPlace
                    .copy(name = newItem.name,
                            summary = newItem.summary,
                            notes = newItem.notes,
                            locationType = newItem.locationType,
                            areaCode = newItem.areaCode,
                            primaryUrl = newItem.primaryUrl,
                            imageUrl = newItem.imageUrl,
                            coordinates = newItem.coordinates,
                            authScope = newItem.authScope,
                            tags = newItem.tags
                    )
            ResponseEntity.ok().body(repo.save(updatedPlace))
        }.orElse(ResponseEntity.notFound().build())
    }


    // https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
    @DeleteMapping("{id}")
    override fun deleteItem(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting place $id")
        return repo.findById(id).map { place ->
            repo.delete(place)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Search all items
     */
    @GetMapping("search/")
    fun searchAll(): List<PlaceSummary> {
        return search("")
    }

    /**
     * Search by search query
     */
    @GetMapping("search/{search}")
    override fun search(@PathVariable(required = true) search: String): List<PlaceSummary> {
        val authScopes = authService.allowedAuthScopesAsString()
        val items = repo.search(search, authScopes)
        log.info("allItemsSearch(${search}) return ${items.size} places authScopes=${authScopes}")
        return items
    }

}
