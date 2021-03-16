package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.repo.TagRepository
import net.timafe.angkor.security.SecurityUtils
import org.springframework.cache.annotation.CacheEvict
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
) : EntityService<Dish, DishSummary, UUID>(repo) {

    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_DISHES_CACHE], allEntries = true)
    override fun save(item: Dish): Dish {
        log.debug("save${entityType()}: $item")
        val autotags = mutableListOf<String>()
        val area = getArea(item.areaCode)
        if (area?.adjectival != null) autotags.add(area.adjectival!!)
        taggingService.mergeAndSort(item, autotags)
        return super.save(item) // leave the actual persistence to the parent
    }


    // just a delegated, but we keep the method here to manage cache expiry
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_DISHES_CACHE], allEntries = true)
    override fun delete(id: UUID) {
        super.delete(id)
    }

    /**
     * Search Item API
     */
    override fun search(search: SearchRequest): List<DishSummary> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val items = repo.search(search.asPageable(), search.query, authScopes)
        log.debug("search${entityType()}s: '$search' ${items.size} results")
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

    override fun entityType(): EntityType {
        return EntityType.DISH
    }
}
