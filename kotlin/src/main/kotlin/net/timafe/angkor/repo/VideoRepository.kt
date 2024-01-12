package net.timafe.angkor.repo

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
interface VideoRepository : CrudRepository<Video, UUID>,AuthScopeSupport<Video>{

    fun findOneByExternalId(externalId: String): Optional<Video>

    // query by authscope should also work with none-native queries:
    @Query("SELECT v FROM Video v WHERE v.authScope IN (:authScopes)")
    override fun findAll(@Param("authScopes") authScopes: List<AuthScope>): List<Video>
}
