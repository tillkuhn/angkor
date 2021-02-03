package net.timafe.angkor.service


import net.timafe.angkor.domain.Place
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
    private val areaService: AreaService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a place.
     *
     * @param place the entity to save.
     * @return the persisted entity.
     */
    fun save(place: Place): Place {
        log.debug("Request to save Place : $place")
        val area = areaService.countriesAndRegions().find { it.code.equals(place.areaCode) }
        if (area != null && (! place.tags.contains(area.name.toLowerCase()))) {
            place.tags = place.tags + area.name.toLowerCase()
            log.info("Adding country mname tag ${place.tags}")
        }
        return repo.save(place)
    }

    /**
     * Get all the places.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): List<Place> {
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
    fun findOne(id: UUID): Optional<Place> {
        log.debug("Request to get Place : $id")
        return repo.findById(id)
    }

    /**
     * Delete the place by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: UUID) {
        log.debug("Request to delete Place : $id")
        repo.deleteById(id)
    }

    /**
     * Return POIs
     */
    fun findPointOfInterests() = repo.findPointOfInterests( SecurityUtils.allowedAuthScopesAsString())
}
