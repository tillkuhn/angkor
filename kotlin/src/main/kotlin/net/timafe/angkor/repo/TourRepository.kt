package net.timafe.angkor.repo

import net.timafe.angkor.domain.Tour
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TourRepository : CrudRepository<Tour, UUID> {

    override fun findAll(): List<Tour>

    // todo add index on name, append OrderByName
    fun findByName(name: String): List<Tour>

}
