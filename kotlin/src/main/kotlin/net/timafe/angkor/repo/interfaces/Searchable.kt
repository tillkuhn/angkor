package net.timafe.angkor.repo.interfaces

import org.springframework.data.domain.Pageable

interface Searchable<EST> {
    fun search(
        pageable: Pageable,
        search: String?,
        authScopes: String
    ): List<EST>
}
