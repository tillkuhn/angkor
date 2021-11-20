package net.timafe.angkor.repo

import net.timafe.angkor.domain.LocatableEntity
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.repo.interfaces.AuthScopeSupport
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Crud Operations for Videos
 * For complex Searches, use LocationSearch
 */
interface LocationRepository : CrudRepository<LocatableEntity, UUID>, AuthScopeSupport<LocatableEntity> {

    // fun findOneByExternalId(externalId: String): Optional<Video>

    @Query(
        """
        SELECT COUNT(*) FROM Place 
        WHERE coordinates != '{}' AND auth_scope=ANY (cast(:authScopes as auth_scope[]))
        """, nativeQuery = true
    )
    fun itemsWithCoordinatesCount(@Param("authScopes") authScopes: String): Long

    // query by authscope should also work with none-native queries:
    @Query("SELECT l FROM LocatableEntity l WHERE l.authScope IN (:authScopes)")
    override fun findAll(@Param("authScopes") authScopes: List<AuthScope>): List<Video>

    // Query by type https://stackoverflow.com/a/4884351/4292075
    // Some missing information: "TYPE" is a "JPQL Special Operator" taking in argument
    // element name (l), and may be compared to the simple class name
    // https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
    @Query("SELECT count(l) FROM LocatableEntity l WHERE TYPE(l) IN (:types) AND l.authScope IN (:authScopes)")
    fun itemCountByTypes(
        @Param("types") entityClasses: List<Class<out LocatableEntity>>,
        @Param("authScopes") authScopes: List<AuthScope>
    ): Long

}
