package net.timafe.angkor.repo

import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.enums.AuthScope
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Crud Operations for Videos
 * For complex Searches, use LocationSearch
 */
interface VideoRepository : CrudRepository<Video, UUID>{

    // fun findOneByExternalId(externalId: String): Optional<Video>

    @Query("SELECT COUNT(v) FROM Video v")
    fun itemCount(): Long

    // query by authscope should also work with none-native queries:
    @Query("SELECT v FROM Video v WHERE v.authScope IN (:authScopes)")
    fun findAllByAuthScope(@Param("authScopes") authScopes: List<AuthScope>): List<Video>
}
