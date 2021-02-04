package net.timafe.angkor.rest

import net.timafe.angkor.domain.interfaces.AuthScoped
import org.springframework.http.ResponseEntity
import java.util.*

/**
 * ET = EntityType, EST EntitySummaryType
 */
interface ResourceController<ET: AuthScoped,EST> {

    fun getItem(id: UUID): ResponseEntity<ET>
    fun deleteItem(id: UUID): ResponseEntity<Void>
    fun createItem(item: ET): ET
    fun updateItem(newItem: ET,id: UUID): ResponseEntity<ET>

    // fun getAll(): List<EST>
    fun search(search: String): List<EST>

}
