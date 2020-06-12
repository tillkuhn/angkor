package org.timafe.p2b

import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import org.timafe.p2b.model.Place

// @EnableScan
interface PlaceRepository : CrudRepository<Place, String> {
    fun findByName(name: String): List<Place>
    override fun findAll(): List<Place>
}
