package net.timafe.angkor.repo

import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.enums.AuthScope
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface TourRepository : CrudRepository<Tour, UUID>{

    fun findOneByExternalId(externalId: String): Optional<Tour>

    @Query("SELECT COUNT(t) FROM Tour t")
    fun itemCount(): Long

    @Query("SELECT t FROM Tour t WHERE t.authScope IN (:authScopes)")
    fun findAllByAuthScope(@Param("authScopes") authScopes: List<AuthScope>): List<Tour>

}
