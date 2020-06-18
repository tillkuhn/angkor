package org.timafe.angkor

import org.springframework.data.repository.CrudRepository
import org.timafe.angkor.model.Place
import java.util.*

// @EnableScan
interface PlaceRepository : CrudRepository<Place, UUID> {
    fun findByName(name: String): List<Place>
    override fun findAll(): List<Place>
}
