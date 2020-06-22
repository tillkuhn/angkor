package net.timafe.angkor.repo

import org.springframework.data.repository.CrudRepository
import net.timafe.angkor.domain.Place
import java.util.*

// @EnableScan
interface PlaceRepository : CrudRepository<Place, UUID> {
    fun findByName(name: String): List<Place>
    override fun findAll(): List<Place>
}
