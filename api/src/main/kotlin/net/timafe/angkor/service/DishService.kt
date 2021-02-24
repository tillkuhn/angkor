package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [Dish].
 */
@Service
@Transactional
class DishService(
    private val repo: DishRepository,
    private val areaService: AreaService,
    private val taggingService: TaggingService
) : EntityService<Dish, DishSummary, UUID> {

    private val log = LoggerFactory.getLogger(javaClass)
    private val entityName = Dish::class.java.simpleName

    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    override fun save(item: Dish): Dish {
        log.debug("save$entityName: $item")
        val autotags = mutableListOf<String>()
        val area = getArea(item.areaCode)
        if (area?.adjectival != null) autotags.add(area.adjectival!!)
        taggingService.mergeAndSort(item, autotags)
        return repo.save(item)
    }

    /**
     * Get all the places.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<Dish> {
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
    override fun findOne(id: UUID): Optional<Dish> {
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
    override fun search(search: SearchRequest): List<DishSummary> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val items = repo.search(search.asPageable(), search.query, authScopes)
        log.debug("search${entityName}s: '$search' ${items.size} results")
        return items
    }

    /**
     * Extract the area from the code (or the parent's code if it's an region)
     */
    private fun getArea(areaCode: String): Area? {
        val area = areaService.countriesAndRegions().find { it.code == areaCode }
        return if (area?.level == AreaLevel.REGION) {
            // resolve to parent
            areaService.countriesAndRegions().find { it.code == area.parentCode }
        } else {
            area
        }
    }
}
