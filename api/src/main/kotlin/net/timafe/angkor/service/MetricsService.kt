package net.timafe.angkor.service

import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.repo.LinkRepository
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.repo.PlaceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MetricsService(
    private val placeRepo: PlaceRepository,
    private val dishRepo: DishRepository,
    private val noteRepo: NoteRepository,
    private val linkRepo: LinkRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Entity Count Statistics
     */
    fun entityStats(): Map<String, Long> {
        val stat: MutableMap<String, Long> = mutableMapOf()
        stat[EntityType.PLACE.path] = placeRepo.itemCount()
        stat[EntityType.NOTE.path] = noteRepo.itemCount()
        stat[EntityType.DISH.path] = dishRepo.itemCount()
        stat[EntityType.VIDEO.path] = linkRepo.videoCount()
        stat["pois"] = placeRepo.itemsWithCoordinatesCount() // should be separate count with e.g. countries POIs on top
        this.log.debug("Current Stats: $stat")
        return stat
    }

}
