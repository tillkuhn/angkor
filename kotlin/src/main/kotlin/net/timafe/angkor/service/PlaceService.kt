package net.timafe.angkor.service


import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.repo.TagRepository
import net.timafe.angkor.security.SecurityUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [Place].
 */
@Service
@Transactional
class PlaceService(
    private val repo: PlaceRepository,
    private val areaService: AreaService,
) : AbstractEntityService<Place, PlaceSummary, UUID>(repo) {


    /**
     * Save a place.
     *
     * @param item the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_PLACES_CACHE], allEntries = true)
    override fun save(item: Place): Place {
        log.trace("save${entityType()}: $item")
        val area = getArea(item.areaCode)
        val autoTags = mutableListOf<String>()
        if (area != null) autoTags.add(area.name)
        TaggingUtils.mergeAndSort(item, autoTags)
        return super.save(item) // Let the superclass do the main work
    }


    // Delegate, but use function as holder for cache annotation
    @CacheEvict(cacheNames = [TagRepository.TAGS_FOR_PLACES_CACHE], allEntries = true)
    override fun delete(id: UUID) = super.delete(id)

    /**
     * Return all POIs visible to the current user
     */
    fun findPointOfInterests() = repo.findPointOfInterests(SecurityUtils.allowedAuthScopesAsString())

    /**
     * Extract the area from the code (or the parent's code if it's a region)
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

    // impl required by superclass
    override fun entityType(): EntityType {
        return EntityType.Place
    }
}
