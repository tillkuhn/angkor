package net.timafe.angkor.repo

import net.timafe.angkor.domain.Tour
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface TourRepository : CrudRepository<Tour, UUID>, Searchable<Tour> {

    override fun findAll(): List<Tour>

    fun findOneByExternalId(externalId: String): Tour?

    // Satisfy interface, but for the time being simply return everything
    // We need to use authScopes in the query, or we get a fatal exception
    // so as a workaround, we use it here even though it's unlikely to ever match t.name
    //
    // Case insensitive search with JPQL: https://newbedev.com/jpql-like-case-insensitive
    @Query("from Tour t  WHERE lower(t.name) LIKE lower(concat('%', :search,'%')) or lower(t.name) like %:authScopes%")
    override fun search(
        pageable: Pageable,
        @Param("search") search: String?,
        @Param("authScopes") authScopes: String
    ): List<Tour>
}
