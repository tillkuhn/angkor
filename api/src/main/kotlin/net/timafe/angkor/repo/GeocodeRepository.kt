package net.timafe.angkor.repo

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.AreaLevel
import org.springframework.data.repository.CrudRepository

interface GeocodeRepository : CrudRepository<Area, String> {
    fun findByName(name: String): List<Area>

    override fun findAll(): List<Area>

    fun findByOrderByName(): List<Area>

    fun findByLevelOrderByName(level: AreaLevel): List<Area>

}
