package net.timafe.angkor.web

import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.AbstractEntityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

/**
 * Base Class for standard entity Controllers
 */
abstract class EntityController<ET, EST, ID>(
    private val service: AbstractEntityService<ET, EST, ID>
) {

    /**
     * A new entity is born
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    open fun create(@Valid @RequestBody item: ET): ET = service.save(item)

    /**
     * Process updates, take only what we want / need
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    fun save(@Valid @RequestBody newItem: ET, @PathVariable id: ID): ResponseEntity<ET> {
        return service.findOne(id).map { currentItem ->
            val updatedItem: ET = mergeUpdates(currentItem, newItem)
            ResponseEntity.ok().body(service.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Must implement, controls which fields are copied
     */
    abstract fun mergeUpdates(currentItem: ET, newItem: ET): ET

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: ID): ResponseEntity<Void> {
        return service.findOne(id).map {
            service.delete(id)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findOne(@PathVariable id: ID): ResponseEntity<ET> {
        return service.findOne(id).map { item ->
            if (accessGranted(item)) ResponseEntity.ok(item!!) else ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build()
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping("search/")
    fun searchAll(): List<EST> {
        return search(SearchRequest()) // Search with default request (empty string)
    }

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<EST> = service.search(search)

    /**
     * If item implements Auth Scopes, consults SecurityUtils if current roles
     * Grant access, else we assume no restrictions
     */
    private fun accessGranted(item: ET): Boolean {
        // If item does not implement Authscope, it's implicitly visible to everbody
        return (item !is AuthScoped) || SecurityUtils.allowedToAccess(item)
    }

}
