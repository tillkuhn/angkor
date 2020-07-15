package net.timafe.angkor.repo

import net.timafe.angkor.domain.POI
import net.timafe.angkor.domain.Place
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PlaceRepository : CrudRepository<Place, UUID> {

    fun findByName(name: String): List<Place>

    override fun findAll(): List<Place>

    fun findByOrderByName(): List<Place>

    @Query("SELECT p FROM Place p where p.authScope = net.timafe.angkor.domain.AuthScope.PUBLIC ORDER BY p.name")
    fun findPublicPlaces(): List<Place>

    // https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity
    //@Query(value = "SELECT p FROM Place p where p.lotype = net.timafe.angkor.domain.LocationType.CITY order by p.name")


    // try SELECT NEW example.CountryAndCapital(c.name, c.capital.name)
    //FROM Country AS c
    // https://stackoverflow.com/questions/52166439/jpa-using-param-values-in-return-for-select
    @Query(value = "SELECT NEW net.timafe.angkor.domain.POI(p.id,p.name,p.coordinates) FROM Place p")
    fun findPointOfInterests(): List<POI>

    // Adhoc queries
    // var query: TypedQuery<Place?>? = em.createQuery("SELECT c FROM Place c where c.lotype=net.timafe.angkor.domain.LocationType.CITY", Place::class.java)
    // val results: List<Place?> = query!!.getResultList()
}
