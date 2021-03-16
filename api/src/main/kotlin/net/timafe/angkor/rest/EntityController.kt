package net.timafe.angkor.rest

import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.EntityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

abstract class EntityController<ET, EST, ID>(
    private val service: EntityService<ET, EST, ID>
) {

    fun create(item: ET): ET  = service.save(item)

    // we need to figure out how to deal with update copy function
    abstract fun save(newItem: ET, id: UUID): ResponseEntity<ET>;

    fun delete(id: ID): ResponseEntity<Void> {
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

    fun searchAll(): List<EST> {
        return search(SearchRequest()) // Search with default request (empty string)
    }

    fun search(search: SearchRequest): List<EST> = service.search(search)

    /**
     * If item implements Auth Scopes, consults SecurityUtils if current roles
     * Grant access, else we assume no restrictions
     */
    private fun accessGranted(item: ET): Boolean {
        return if (item is AuthScoped) SecurityUtils.allowedToAccess(item) else true
    }

}
