package net.timafe.angkor.service


import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [Note].
 */
@Service
@Transactional
class NoteService(
    private val appProperties: AppProperties,
    private val repo: NoteRepository,
    private val taggingService: TaggingService
) : EntityService<Note, NoteSummary, UUID> {

    private val log = LoggerFactory.getLogger(javaClass)
    private val entityName = Note::class.java.simpleName

    companion object {
        // todo move to database ...
        val urlToTag = mapOf<String, Array<String>>(
            "watch" to arrayOf("zdf.de", "youtube"),
            "dish" to arrayOf("chefkoch", "asiastreetfood")
        )
    }

    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    override fun save(item: Note): Note {
        log.debug("save$entityName: $item")
        val autotags = mutableListOf<String>()
        if (item.primaryUrl != null) {
            for ((tag, urlPatterns) in urlToTag) {
                urlPatterns.forEach { urlPattern ->
                    if (item.primaryUrl!!.toLowerCase().contains(urlPattern)) {
                        autotags.add(tag)
                    }
                }
            }
        }
        taggingService.mergeAndSort(item, autotags)
        // if (area != null) taggingService.mergeTags(item,area.name)
        return repo.save(item)
    }

    /**
     * Get all the places.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<Note> {
        val items = repo.findAll()
        log.debug("findAll${entityName}s: ${items.size} results")
        return items
    }

    /**
     * Get one place by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: UUID): Optional<Note> {
        val item = repo.findById(id)
        log.debug("findOne$entityName: $id found=${item.isPresent}")
        return item
    }

    /**
     * Delete the place by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: UUID) {
        log.debug("delete$entityName: $id")
        repo.deleteById(id)
    }

    /**
     * Search Item API
     */
    override fun search(search: SearchRequest): List<NoteSummary> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val items = repo.search(search.asPageable(), search.query, authScopes)
        log.debug("search${entityName}s: '$search' ${items.size} results")
        return items
    }

    // Custom Entity Specific Operations
    fun noteReminders(): List<NoteSummary> {
        val items = repo.noteReminders(appProperties.externalBaseUrl)
        log.debug("reminders: ${items.size} results")
        return items
    }

}
