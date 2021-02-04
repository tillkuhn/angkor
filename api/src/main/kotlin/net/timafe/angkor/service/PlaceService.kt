package net.timafe.angkor.service


import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.PlaceSummary
import net.timafe.angkor.repo.PlaceRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
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
    private val taggingService: TaggingService
): EntityService<Place,PlaceSummary,UUID> {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a place.
     *
     * @param place the entity to save.
     * @return the persisted entity.
     */
    override fun save(place: Place): Place {
        log.debug("Request to save Place: $place")
        val area = areaService.countriesAndRegions().find { it.code == place.areaCode }
        if (area != null) taggingService.mergeTags(place,area.name)
        return repo.save(place)
    }

    /**
     * Get all the places.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<Place> {
        log.debug("Request to get all Places")
        return repo.findAll()
    }

    /**
     * Get one place by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: UUID): Optional<Place> {
        log.debug("Request to get Place: $id")
        return repo.findById(id)
    }

    /**
     * Delete the place by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: UUID) {
        log.debug("Request to delete Place : $id")
        repo.deleteById(id)
    }

    /**
     * Return POIs
     */
    fun findPointOfInterests() = repo.findPointOfInterests( SecurityUtils.allowedAuthScopesAsString())

    /**
     * Search API
     */
    override fun search(search: String): List<PlaceSummary> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        return repo.search(search,authScopes)
    }
}
