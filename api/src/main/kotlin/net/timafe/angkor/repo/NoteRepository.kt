package net.timafe.angkor.repo

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface NoteRepository : CrudRepository<Note, UUID> {

    override fun findAll(): List<Note>

    /**
     * Main Search Query for taggable items, implemented as nativeQuery to support complex matching
     */
    @Query(value = """
    SELECT cast(id as text), summary, status as status,auth_scope as authScope,
        to_char(created_at, 'YYYY-MM-DD"T"HH24:MI:SSOF') as createdAt,
        to_char(due_date, 'YYYY-MM-DD') as dueDate,
        primary_url as primaryUrl,
        cast(tags as text) as tags
    FROM note 
    WHERE (summary ILIKE %:search% or text_array(tags) ILIKE %:search%)
       AND auth_scope= ANY (cast(:authScopes as auth_scope[]))
    ORDER BY created_at DESC
    LIMIT :limit
    """, nativeQuery = true)
    fun search(@Param("search") search: String?,
               @Param("authScopes") authScopes: String,
               @Param("limit") limit: Int = Constants.JPA_DEFAULT_RESULT_LIMIT): List<NoteSummary>

    @Query(value = """
    SELECT cast(id as text), summary, status as status,auth_scope as authScope,
        to_char(due_date, 'YYYY-MM-DD') as dueDate,
        primary_url as primaryUrl,
        cast(tags as text) as tags
    FROM note 
    WHERE ( due_date <= now())
    LIMIT :limit
    """, nativeQuery = true)
    fun noteReminders(@Param("limit") limit: Int = Constants.JPA_DEFAULT_RESULT_LIMIT): List<NoteSummary>

//    select n.id,n.summary,n.auth_scope,n.created_by,n.status,n.due_date,tags,n.primary_url,
//    u.name,u.email
//    from note n
//    left join app_user u on n.created_by = u.id
//    where due_date <= now()
//    order by due_date asc

}
