package net.timafe.angkor.repo

import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.repo.interfaces.AuthScopeSupport
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Spring Data  repository for the Location [Place] entity.
 */
interface PlaceRepository : CrudRepository<Place, UUID>, AuthScopeSupport<Place> {

    // override fun findAll(): List<Place> // iterator -> list cast no longer needed ?
    // query by authscope should also work with none-native queries:
    @Query("SELECT p FROM Place p WHERE p.authScope IN (:authScopes)")
    override fun findAll(@Param("authScopes") authScopes: List<AuthScope>): List<Place>
}


// interface PlaceRepository : CrudRepository<Place, UUID>, Searchable<PlaceSummary> {
//    /**
//     * Return a list of POIs, which are basically coordinates with some basic info on Mappable
//     * https://stackoverflow.com/questions/52166439/jpa-using-param-values-in-return-for-select
//     */
//    @Query(
//        value = """
//        SELECT cast(id as text),name,area_code as areaCode,image_url as imageUrl,
//               location_type as locationType,
//               cast(coordinates as text) as coordinates
//        FROM Place
//        WHERE auth_scope=ANY (cast(:authScopes as auth_scope[]))
//        """, nativeQuery = true
//    )
//    fun findPointOfInterests(@Param("authScopes") authScopes: String): List<POI>

//    /**
//     * Main Search Query for taggable items, implemented as nativeQuery to support complex matching
//     */
//    @Query(
//        value = """
//    SELECT cast(id as text),name,summary,area_code as areaCode,primary_url as primaryUrl,
//        auth_scope as authScope, location_type as locationType,
//        to_char(updated_at, 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') as updatedAt,
//        cast(tags as text) as tags,
//        cast(coordinates as text) as coordinates
//    FROM place
//    WHERE (name ILIKE %:search% or summary ILIKE %:search% or text_array(tags) ILIKE %:search%)
//       AND auth_scope=ANY (cast(:authScopes as auth_scope[]))
//    """, nativeQuery = true
//    )
//    override fun search(
//        pageable: Pageable,
//        @Param("search") search: String?,
//        @Param("authScopes") authScopes: String
//        /*@Param("limit") limit: Int = Constants.JPA_DEFAULT_RESULT_LIMIT*/
//    ): List<PlaceSummary>

//    @Query("SELECT COUNT(p) FROM Place p")
//    fun itemCount(): Long

    // No longer used, we use location count instead now

//    @Query("SELECT COUNT(*) FROM Place where coordinates != '{}'", nativeQuery = true)
//    fun itemsWithCoordinatesCount(): Long

