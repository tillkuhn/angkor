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
    // We need to use authScopes in the query or we get a fatal exception
    // But we don't support it yet in none-native queries ...
    // so this is an ugly workaround to use it in the query wihout limiting it
    // since imageUrl will never resemble any authscope list  :-)
    @Query("from Tour t  WHERE lower(t.name) LIKE %:search% and t.imageUrl not like %:authScopes%")
    override fun search(
        pageable: Pageable,
        @Param("search") search: String?,
        @Param("authScopes") authScopes: String
    ): List<Tour>
}
