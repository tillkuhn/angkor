package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.interfaces.AuthScopeSupport
import net.timafe.angkor.repo.interfaces.Searchable
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Superclass for standard entity services
 */
// Quick fix to add `Any` as an upper bound for type parameter ET to make it non-nullable
// https://youtrack.jetbrains.com/issue/KT-36770/ Prohibit unsafe calls with expected @NotNull
// https://youtrack.jetbrains.com/issue/KTIJ-20557 Quick fix to add `Any` as an upper bound
// Quick fix to replace `@NotNull` parameter type `T` with a definitely non-nullable type `T & Any`
// https://youtrack.jetbrains.com/issue/KTIJ-20425/
abstract class AbstractEntityService<ET: Any, EST, ID>(
    private val repo: CrudRepository<ET, ID>,
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
        return this.repo.save(item) // Throw NPE is OK as ID is mandatory, otherwise we get compiler warning
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
        val items = if (repo is AuthScopeSupport<*>) {
            @Suppress("UNCHECKED_CAST") // todo do better
            repo.findAll(SecurityUtils.allowedAuthScopes()) as List<ET>
        } else {
            repo.findAll().toList() // CrudRepository "only" returns Iterable
        }
        log.debug("${logPrefix()} findAll:  ${items.size} results")
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
            throw UnsupportedOperationException("$repo does not implement Searchable interface")
        }
    }

    /**
     * Returns a common prefix for log messages that represents the Entity, e.g. [ Tour ]
     */
    protected fun logPrefix() = "[${
        entityType().name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }]"

}
