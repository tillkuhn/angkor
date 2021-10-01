package net.timafe.angkor.repo

import net.timafe.angkor.domain.Tour
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TourRepository : CrudRepository<Tour, UUID>, Searchable<Tour> {

    override fun findAll(): List<Tour>

    fun findByName(name: String?): List<Tour>
    fun findOneByExternalId(externalId: String): Tour?

    // Satisfy interface, but for the time being simply return everything
    @Query("from Tour t")
    override fun search(
        pageable: Pageable,
        search: String?,
        authScopes: String
    ): List<Tour>
}
