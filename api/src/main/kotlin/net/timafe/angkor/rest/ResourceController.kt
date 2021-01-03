package net.timafe.angkor.rest

import net.timafe.angkor.domain.AuthScoped
import org.springframework.http.ResponseEntity
import java.util.*

/**
 * T = EntityType, ST SummaryType
 */
interface ResourceController<T: AuthScoped,ST> {

    fun getItem(id: UUID): ResponseEntity<T>
    fun deleteItem(id: UUID): ResponseEntity<Void>
    fun createItem(item: T): T
    fun updateItem(newItem: T,id: UUID): ResponseEntity<T>

    // fun getAll(): List<ST>
    fun search(search: String): List<ST>

}
