package org.timafe.angkor

import org.springframework.data.repository.CrudRepository
import org.timafe.angkor.model.Place

// @EnableScan
interface PlaceRepository : CrudRepository<Place, String> {
    fun findByName(name: String): List<Place>
    override fun findAll(): List<Place>
}
