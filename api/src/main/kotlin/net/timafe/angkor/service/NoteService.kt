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
    private val taggingService: TaggingService,
) : EntityService<Note, NoteSummary, UUID>(repo) {

    /**
     * Experimental, should be moved to Tag Entity and persisted in DB
     */
    companion object {
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
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_NOTES_CACHE], allEntries = true)
    override fun save(item: Note): Note {
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
        return super.save(item)
    }

    /**
     * Delete the place by id.
     *
     * @param id the id of the entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_NOTES_CACHE], allEntries = true)
    override fun delete(id: UUID) = super.delete(id)

    // Custom Entity Specific Operations
    fun noteReminders(): List<NoteSummary> {
        val items = repo.noteReminders(appProperties.externalBaseUrl)
        log.debug("reminders: ${items.size} results")
        return items
    }

    override fun entityType(): EntityType {
        return EntityType.NOTE
    }

}
