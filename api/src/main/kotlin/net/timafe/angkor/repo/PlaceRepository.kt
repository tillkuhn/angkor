package net.timafe.angkor.repo

import org.springframework.data.repository.CrudRepository
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.POI
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PlaceRepository : CrudRepository<Place, UUID> {
    fun findByName(name: String): List<Place>
    override fun findAll(): List<Place>

    // try SELECT NEW example.CountryAndCapital(c.name, c.capital.name)
    //FROM Country AS c
    // https://stackoverflow.com/questions/52166439/jpa-using-param-values-in-return-for-select
    @Query(value = "SELECT NEW net.timafe.angkor.domain.POI(p.id,p.name,p.coordinates) FROM Place p")
    fun findPointOfInterests(): List<POI>
}
