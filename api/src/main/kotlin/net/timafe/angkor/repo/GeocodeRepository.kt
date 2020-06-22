package net.timafe.angkor.repo

import net.timafe.angkor.domain.Geocode
import org.springframework.data.repository.CrudRepository

interface GeocodeRepository : CrudRepository<Geocode, String> {
    fun findByName(name: String): List<Geocode>
    override fun findAll(): List<Geocode>
}
