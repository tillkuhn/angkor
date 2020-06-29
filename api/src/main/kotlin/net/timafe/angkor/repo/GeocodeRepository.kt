package net.timafe.angkor.repo

import net.timafe.angkor.domain.Geocode
import net.timafe.angkor.domain.GeocodeLevel
import net.timafe.angkor.domain.LocationType
import org.springframework.data.repository.CrudRepository

interface GeocodeRepository : CrudRepository<Geocode, String> {
    fun findByName(name: String): List<Geocode>

    override fun findAll(): List<Geocode>

    fun findByOrderByName(): List<Geocode>

    fun findByLevelOrderByName(level: GeocodeLevel): List<Geocode>

}
