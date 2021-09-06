package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.Searchable
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Superclass for standard entity services
 */
abstract class AbstractEntityService<ET, EST, ID>(
    private val repo: CrudRepository<ET, ID>
) {

    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun entityType(): EntityType

    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    open fun save(item: ET): ET {
        this.log.info("${logPrefix()} Save $item")
        return this.repo.save(item!!) // Throw NPE is OK as ID is mandatory, otherwise we get compiler warning
    }

    /**
     * Delete the place by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    open fun delete(id: ID) {
        this.repo.deleteById(id!!) // Throw NPE is OK as ID is mandatory, otherwise we get compiler warning
        this.log.info("${logPrefix()} Delete id=$id: successful")
    }

    /**
     * Get all existing items.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    open fun findAll(): List<ET> {
        val items = repo.findAll().toList() // CrudRepository "only" returns Iterable
        this.log.info("${logPrefix()} FindAll: ${items.size} results")
        return items
    }

    /**
     * Get one item by id.
     *
     * @param id the id of the entity.
     * @return the entity wrapped as Optional.
     */
    @Transactional(readOnly = true)
    open fun findOne(id: ID): Optional<ET> {
        val item = if (id != null) repo.findById(id) else Optional.empty()
        log.debug("${logPrefix()} FindOne: id=$id found=${item.isPresent}")
        return item
    }

    /**
     * Run a search query against the repo
     */
    @Transactional(readOnly = true)
    open fun search(search: SearchRequest): List<EST> {
        if (repo is Searchable<*>) {
            val authScopes = SecurityUtils.allowedAuthScopesAsString()
            val items = repo.search(search.asPageable(), search.query, authScopes)
            log.debug("${logPrefix()} Search '$search': ${items.size} results")
            @Suppress("UNCHECKED_CAST")
            // or study https://stackoverflow.com/questions/36569421/kotlin-how-to-work-with-list-casts-unchecked-cast-kotlin-collections-listkot
            return items as List<EST>
        } else {
            throw UnsupportedOperationException("$repo does not implement searchable")
        }
    }
    
    fun logPrefix() = "[${entityType().name.lowercase().capitalize()}]"

}
