package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Tag
import net.timafe.angkor.domain.dto.TagSummary
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.TagRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing [Tag].
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/tags")
class TagController(
    private val repository: TagRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get all details of a single place
     */
    @GetMapping("{entityType}")
    @ResponseStatus(HttpStatus.OK)
    fun getEntityTags(@PathVariable entityType: String): List<TagSummary> {
        val et = EntityType.valueOf(entityType.uppercase())
        log.trace("Retrieve Tags for Entity $et")
        return when (et) {
            EntityType.DISH -> repository.findTagsForDishes()
            EntityType.NOTE -> repository.findTagsForNotes()
            EntityType.PLACE -> repository.findTagsForPlaces()
            else -> throw IllegalArgumentException("$entityType is not a support entityType")
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun alltags(): List<Tag> {
        return repository.findByOrderByLabel()
    }

}
