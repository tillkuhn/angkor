package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.MetricsService
import net.timafe.angkor.service.PlaceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

/**
 * REST controller for managing [Place].
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/" + Constants.API_PATH_PLACES)
class PlaceController(
    var service: PlaceService,
    var stats: MetricsService
) : ResourceController<Place, PlaceSummary> {

    /**
     * Get all details of a single place
     */
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getItem(@PathVariable id: UUID): ResponseEntity<Place> {
        return service.findOne(id).map { item ->
            if (SecurityUtils.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build()
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
        return service.findOne(id).map { existingItem ->
            val updatedItem: Place = existingItem
                .copy(
                    name = newItem.name,
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
        return service.findOne(id).map {
            service.delete(id)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Search all items
     */
    @GetMapping("search/")
    fun searchAll(): List<PlaceSummary> {
        return search(SearchRequest()) // Search with default request (empty string)
    }

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    override fun search(@Valid @RequestBody search: SearchRequest): List<PlaceSummary> = service.search(search)

}
