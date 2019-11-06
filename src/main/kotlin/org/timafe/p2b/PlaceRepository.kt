package org.timafe.p2b

import org.springframework.data.repository.CrudRepository
import org.socialsignin.spring.data.dynamodb.repository.EnableScan

@EnableScan
interface PlaceRepository : CrudRepository<Place, String> {
        fun findByName(name: String): List<Place>

        override fun findAll() : List<Place>
}
