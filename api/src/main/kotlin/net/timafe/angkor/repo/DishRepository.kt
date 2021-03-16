package net.timafe.angkor.repo

import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface DishRepository : CrudRepository<Dish, UUID>, Searchable<DishSummary> {

    // @Query("SELECT d FROM Dish d WHERE d.authScope IN (:authScopes)")
    // fun findDishesByAuthScope(@Param("authScopes") authScopes: List<AuthScope>): List<Dish>

    // -- http://www.seanbehan.com/how-to-cast-a-string-of-comma-separated-numbers-into-an-array-of-integers-for-postgres/
    //select * from dish where auth_scope = ANY ('{"ALL_AUTH","PUBLIC"}'::auth_scope[])

    @Query(
        value = """
    SELECT cast(id as text),name,summary,area_code as areaCode,primary_url as primaryUrl, auth_scope as authScope,rating,
      to_char(updated_at, 'YYYY-MM-DD"T"HH24:MI:SS') as updatedAt,
      cast(tags as text) as tags
    FROM dish WHERE (name ILIKE %:search% or summary ILIKE %:search% or text_array(tags) ILIKE %:search%)
     AND auth_scope= ANY (cast(:authScopes as auth_scope[]))
    """, nativeQuery = true
    )
    override fun search(
        pageable: Pageable,
        @Param("search") search: String?,
        @Param("authScopes") authScopes: String
    ): List<DishSummary>

    @Query("SELECT COUNT(d) FROM Dish d")
    fun itemCount(): Long
}
