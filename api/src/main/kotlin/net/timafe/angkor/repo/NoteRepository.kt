package net.timafe.angkor.repo

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface NoteRepository : CrudRepository<Note, UUID> {

    override fun findAll(): List<Note>

    /**
     * Main Search Query for taggable items, implemented as nativeQuery to support complex matching
     */
    @Query(
        value = """
    SELECT cast(id as text), summary, status as status,auth_scope as authScope,
        to_char(created_at, 'YYYY-MM-DD"T"HH24:MI:SS') as createdAt,
        to_char(due_date, 'YYYY-MM-DD') as dueDate,
        primary_url as primaryUrl,
        cast(assignee as text) as assignee,
        cast(tags as text) as tags
    FROM note 
    WHERE (summary ILIKE %:search% or text_array(tags) ILIKE %:search%)
       AND auth_scope= ANY (cast(:authScopes as auth_scope[]))
    """, nativeQuery = true
    )
    fun search(
        pageable: Pageable,
        @Param("search") search: String?,
        @Param("authScopes") authScopes: String
    ): List<NoteSummary>

    /**
     * Reminders, custom NoteSummary for Remindabot API and similar use cases with embedded user info
     */
    @Query(
        value = """
    SELECT cast(n.id as text), n.summary, n.status as status,n.auth_scope as authScope,
           to_char(n.due_date, 'YYYY-MM-DD') as dueDate,
           n.primary_url as primaryUrl,
           cast(n.tags as text) as tags,
           u.name as userName,u.email as userEmail,
           :baseUrl || '/notes/' || n.id as noteUrl
    FROM note n
    LEFT JOIN app_user u on n.assignee = u.id
    WHERE ( due_date <= now()) and n.status != 'CLOSED'
    ORDER BY due_date asc
    LIMIT :limit
    """, nativeQuery = true
    )
    fun noteReminders(
        @Param("baseUrl") baseUrl: String,
        @Param("limit") limit: Int = Constants.JPA_DEFAULT_RESULT_LIMIT
    ): List<NoteSummary>

    @Query("SELECT COUNT(n) FROM Note n")
    fun itemCount(): Long
}
