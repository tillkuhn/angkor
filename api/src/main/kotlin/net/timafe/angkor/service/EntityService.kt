package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.SearchRequest
import java.util.*

/**
 * Make interfaces for entity services more consistent
 */
interface EntityService<ET, EST, ID> {

    fun save(item: ET): ET

    fun findAll(): List<ET>

    fun findOne(id: ID): Optional<ET>

    fun delete(id: ID)

    fun search(search: SearchRequest): List<EST>
}
