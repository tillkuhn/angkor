package net.timafe.angkor.repo

import net.timafe.angkor.domain.Location
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
interface LocationRepository : CrudRepository<Location, UUID>,AuthScopeSupport<Location>{

    // fun findOneByExternalId(externalId: String): Optional<Video>

    @Query("SELECT COUNT(l) FROM Location l")
    fun itemCount(): Long

    // query by authscope should also work with none-native queries:
    @Query("SELECT l FROM Location l WHERE l.authScope IN (:authScopes)")
    override fun findAll(@Param("authScopes") authScopes: List<AuthScope>): List<Video>

    // query by type https://stackoverflow.com/a/4884351/4292075
    // Some missing information: "TYPE" is a "JPQL Special Operator" taking in argument
    // element name (l), and may be compared to the simple class name
    // https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
    @Query("SELECT count(l) FROM Location l WHERE TYPE(l) IN (:types) and l.authScope IN (:authScopes)")
    fun itemCountByTypes(
        @Param("types") entityClasses: List<Class<out Location>>,
        @Param("authScopes") authScopes: List<AuthScope>
    ): Long

}
