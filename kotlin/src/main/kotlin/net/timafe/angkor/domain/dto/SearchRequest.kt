package net.timafe.angkor.domain.dto

import net.timafe.angkor.config.Constants
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class SearchRequest(

    var query: String = "",
    var page: Int = 0,
    var pageSize: Int = Constants.JPA_DEFAULT_RESULT_LIMIT,
    var sortDirection: Sort.Direction = Sort.DEFAULT_DIRECTION,
    var sortProperties: MutableList<String> = mutableListOf()

) {

    /**
     * Depending on whether at least one sort Property Exists, we return either an unsorted or
     * a sorted PageRequest Object. Note the "search" string is still stored outside the Pageable
     *
     * use var pageable: Pageable = Pageable.unpaged() if you don't need paging / sorting at all
     * but need to pass in something
     */
    fun asPageable(): Pageable {
        return if (sortProperties.size < 1) PageRequest.of(page, pageSize) else
            PageRequest.of(page, pageSize, sortDirection, *sortProperties.toTypedArray()) // * converts to varargs
    }
}

