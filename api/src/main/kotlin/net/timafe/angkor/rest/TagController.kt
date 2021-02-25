package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Tag
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.dto.TagSummary
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.TagRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.Valid

/**
 * REST controller for managing [Tag].
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/" + Constants.API_PATH_TAGS)
class TagController(
    var repository: TagRepository
)  {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    // private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get all details of a single place
     */
    @GetMapping("{entityType}")
    @ResponseStatus(HttpStatus.OK)
    fun getEntityTags(@PathVariable entityType: String): List<TagSummary> {
        val et = EntityType.valueOf(entityType.toUpperCase());
        this.log.info("Getting tags for $et");
        when (et) {
            EntityType.DISH -> return repository.dishTags()
            EntityType.NOTE -> return repository.noteTags()
            EntityType.PLACE -> return repository.placeTags()
            else -> throw IllegalArgumentException("${entityType} is not a support entityType")
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun alltags(): List<Tag> {
        return repository.findByOrderByLabel()
    }
}
