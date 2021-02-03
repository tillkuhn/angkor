package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.security.AuthService
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.PlaceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

/**
 * REST controller for managing [Place].
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/"+ Constants.API_PATH_PLACES)
class PlaceController(
        var repo: PlaceRepository,
        var service: PlaceService
): ResourceController<Place,PlaceSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get all details of a single place
     */
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getItem(@PathVariable id: UUID): ResponseEntity<Place> {
        return repo.findById(id).map { item ->
            if (SecurityUtils.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Post a new place
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    override fun createItem(@RequestBody item: Place): Place = service.save(item)

    /**
     * Updates an item, this operation needs to be adapted if we add new attributes
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun updateItem(@Valid @RequestBody newItem: Place, @PathVariable id: UUID): ResponseEntity<Place> {
        log.info("update () called for item $id")
        return repo.findById(id).map { existingItem ->
            val updatedItem: Place = existingItem
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
            ResponseEntity.ok().body(service.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }


    // https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
    @DeleteMapping("{id}")
    override fun deleteItem(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting item $id")
        return repo.findById(id).map { place ->
            service.delete(id)
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
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val items = repo.search(search, authScopes)
        log.info("allItemsSearch(${search}) return ${items.size} places authScopes=${authScopes}")
        return items
    }

}
