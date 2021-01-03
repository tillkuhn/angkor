package net.timafe.angkor.repo

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.domain.dto.PlaceSummary
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface PlaceRepository : CrudRepository<Place, UUID> {

    fun findByName(name: String): List<Place>

    override fun findAll(): List<Place>

    /**
     * Return a list of POIs, which are basically coordinates with some basic info on Mappable
     */
    // https://stackoverflow.com/questions/52166439/jpa-using-param-values-in-return-for-select
    @Query(value = """
    SELECT cast(id as text),name,area_code as areaCode,image_url as imageUrl,
           location_type as locationType,
           cast(coordinates as text) as coordinates
    FROM Place
    WHERE auth_scope=ANY (cast(:authScopes as auth_scope[]))
    """, nativeQuery = true)
    fun findPointOfInterests( @Param("authScopes") authScopes: String): List<POI>

    /**
     * Main Search Query for taggable items, implemented as nativeQuery to support complex matching
     */
    @Query(value = """
    SELECT cast(id as text),name,summary,area_code as areaCode,primary_url as primaryUrl,
        auth_scope as authScope, location_type as locationType, 
        to_char(updated_at, 'YYYY-MM-DD"T"HH24:MI:SSOF') as updatedAt,
        cast(tags as text) as tags, 
        cast(coordinates as text) as coordinates
    FROM place 
    WHERE (name ILIKE %:search% or summary ILIKE %:search% or text_array(tags) ILIKE %:search%)
       AND auth_scope=ANY (cast(:authScopes as auth_scope[]))
    LIMIT :limit
    """, nativeQuery = true)
    fun search(@Param("search") search: String?,
               @Param("authScopes") authScopes: String,
               @Param("limit") limit: Int = Constants.JPA_DEFAULT_RESULT_LIMIT): List<PlaceSummary>

    // https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity
    //@Query(value = "SELECT p FROM Place p where p.lotype = net.timafe.angkor.domain.enums.LocationType.CITY order by p.name")

    // Adhoc queries
    // var query: TypedQuery<Place?>? = em.createQuery("SELECT c FROM Place c where c.lotype=net.timafe.angkor.domain.enums.LocationType.CITY", Place::class.java)
    // val results: List<Place?> = query!!.getResultList()


}
