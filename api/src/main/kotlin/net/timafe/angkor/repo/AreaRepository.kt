package net.timafe.angkor.repo

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.enums.AreaLevel
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AreaRepository : CrudRepository<Area, String> {

    companion object {
        const val COUNTRIES_AND_REGIONS_CACHE = "countriesAndRegions"
    }

    fun findByName(name: String): List<Area>

    override fun findAll(): List<Area>

    fun findByOrderByName(): List<Area>

    fun findAll(sort: Sort): List<Area>

    fun findByLevelOrderByName(level: AreaLevel): List<Area>

    @Cacheable(COUNTRIES_AND_REGIONS_CACHE)
    @Query("SELECT a FROM Area a where a.level IN(net.timafe.angkor.domain.enums.AreaLevel.COUNTRY,net.timafe.angkor.domain.enums.AreaLevel.REGION) ORDER BY a.name")
    fun findAllCountriesAndRegions(): List<Area>

}
