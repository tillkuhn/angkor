package net.timafe.angkor.rest

import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.interfaces.AuthScoped
import org.springframework.http.ResponseEntity
import java.util.*

/**
 * ET = EntityType, EST EntitySummaryType
 */
interface ResourceController<ET : AuthScoped, EST> {

    fun create(item: ET): ET
    fun save(newItem: ET, id: UUID): ResponseEntity<ET>
    fun delete(id: UUID): ResponseEntity<Void>
    fun findOne(id: UUID): ResponseEntity<ET>
    fun search(search: SearchRequest): List<EST>
    // fun getAll(): List<EST>
    // fun search(search: String): List<EST> // deprecated

}
