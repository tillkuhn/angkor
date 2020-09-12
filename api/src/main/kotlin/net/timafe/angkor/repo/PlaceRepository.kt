package net.timafe.angkor.repo

import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.domain.dto.PlaceSummary
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PlaceRepository : CrudRepository<Place, UUID> {

    fun findByName(name: String): List<Place>

    override fun findAll(): List<Place>

    /**
     * Returns all places, irrespective of AuthScope
     */
    @Query("""
     SELECT NEW net.timafe.angkor.domain.dto.PlaceSummary(p.id, p.name, p.summary, p.areaCode, p.primaryUrl, p.locationType, p.coordinates) 
     FROM Place p ORDER BY p.name
    """)
    fun findAllPlacesOrderByName(): List<PlaceSummary>

    /**
     * Returs all places that are public (i.e. save to display to anonymous users)
     */
    @Query("""
     SELECT NEW net.timafe.angkor.domain.dto.PlaceSummary(p.id, p.name, p.summary, p.areaCode, p.primaryUrl, p.locationType, p.coordinates) 
     FROM Place p where p.authScope = net.timafe.angkor.domain.enums.AuthScope.PUBLIC ORDER BY p.name
    """)
    fun findPublicPlaces(): List<PlaceSummary>

    // https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity
    //@Query(value = "SELECT p FROM Place p where p.lotype = net.timafe.angkor.domain.enums.LocationType.CITY order by p.name")


    // https://stackoverflow.com/questions/52166439/jpa-using-param-values-in-return-for-select
    @Query(value = "SELECT NEW net.timafe.angkor.domain.dto.POI(p.id,p.name,p.areaCode,p.coordinates) FROM Place p")
    fun findPointOfInterests(): List<POI>

    // Adhoc queries
    // var query: TypedQuery<Place?>? = em.createQuery("SELECT c FROM Place c where c.lotype=net.timafe.angkor.domain.enums.LocationType.CITY", Place::class.java)
    // val results: List<Place?> = query!!.getResultList()
}
