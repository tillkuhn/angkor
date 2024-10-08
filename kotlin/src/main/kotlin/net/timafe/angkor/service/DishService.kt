package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.enums.Area_Level
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.repo.TagRepository
import net.timafe.angkor.service.utils.TaggingUtils
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
    repo: DishRepository, // only used by superclass
    private val areaService: AreaService,
    private val eventService: EventService,
) : AbstractEntityService<Dish, DishSummary, UUID>(repo) {

    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_DISHES_CACHE], allEntries = true)
    override fun save(item: Dish): Dish {
        log.debug("save{}: {}", entityType(), item)
        val autoTags = mutableListOf<String>()
        val area = getArea(item.areaCode)
        if (area?.adjectival != null) autoTags.add(area.adjectival!!)
        TaggingUtils.mergeAndSort(item, autoTags)
        return super.save(item) // leave the actual persistence to the parent
    }

    fun justServed(item: Dish): Int {
        item.timesServed = item.timesServed.inc()
        super.save(item)

        val em = Event(
            entityId = item.id,
            action = "serve:dish",
            message = "Dish ${item.name} was served, new count=${item.timesServed}",
            source = this.javaClass.simpleName)
        eventService.publish(EventTopic.APP, em)
        val newCount = item.timesServed.toInt()
        log.info("New timesServed Count $newCount")
        return newCount
    }

    // just a delegated, but we keep the method here to manage cache expiry
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_DISHES_CACHE], allEntries = true)
    override fun delete(id: UUID) {
        super.delete(id)
    }

    /**
     * Extract the area from the code (or the parent's code if it's a region)
     */
    private fun getArea(areaCode: String): Area? {
        val area = areaService.countriesAndRegions().find { it.code == areaCode }
        return if (area?.level == Area_Level.REGION) {
            // resolve to parent
            areaService.countriesAndRegions().find { it.code == area.parentCode }
        } else {
            area
        }
    }

    override fun entityType(): EntityType {
        return EntityType.Dish
    }
}
