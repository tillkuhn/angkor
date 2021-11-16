package net.timafe.angkor.service


import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.repo.TagRepository
import org.springframework.cache.annotation.CacheEvict
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
) : AbstractEntityService<Note, NoteSummary, UUID>(repo) {

    /**
     * Experimental input map for auto tagging, should be moved to Tag Entity and persisted in DB
     */
    companion object {
        val urlToTag = mapOf(
            "watch" to arrayOf("zdf.de", "youtube", "www.arte.tv"),
            "dish" to arrayOf("chefkoch", "asiastreetfood")
        )
    }

    /**
     * Save a note with autotag support.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_NOTES_CACHE], allEntries = true)
    override fun save(item: Note): Note {
        val autotags = mutableListOf<String>()
        if (item.primaryUrl != null) {
            for ((tag, urlPatterns) in urlToTag) {
                urlPatterns.forEach { urlPattern ->
                    if (item.primaryUrl!!.lowercase().contains(urlPattern)) {
                        autotags.add(tag)
                    }
                }
            }
        }
        TaggingUtils.mergeAndSort(item, autotags)
        return super.save(item)
    }

    /**
     * Delete the note  by id.
     *
     * @param id the id of the entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_NOTES_CACHE], allEntries = true)
    override fun delete(id: UUID) = super.delete(id)

    // Custom Entity Specific Operations
    @Transactional(readOnly = true)
    fun noteReminders(): List<NoteSummary> {
        log.debug("reminders: retrieving from db")
        val items = repo.noteReminders(appProperties.externalBaseUrl)
        log.debug("reminders: found ${items.size} results")
        return items
    }

    // required by superclass
    override fun entityType(): EntityType {
        return EntityType.Note
    }

}
