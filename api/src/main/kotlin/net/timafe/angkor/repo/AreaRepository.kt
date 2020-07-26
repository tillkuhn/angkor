package net.timafe.angkor.repo

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.enums.AreaLevel
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AreaRepository : CrudRepository<Area, String> {
    fun findByName(name: String): List<Area>

    override fun findAll(): List<Area>

    fun findByOrderByName(): List<Area>

    fun findByLevelOrderByName(level: AreaLevel): List<Area>

    @Query("SELECT a FROM Area a where a.level IN(net.timafe.angkor.domain.enums.AreaLevel.COUNTRY,net.timafe.angkor.domain.enums.AreaLevel.REGION) ORDER BY a.name")
    fun findAllAcountiesAndregions(): List<Area>

}
