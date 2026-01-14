package net.timafe.angkor.service

import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.repo.LinkRepository
import net.timafe.angkor.repo.NoteRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MetricsService(
    private val dishRepo: DishRepository,
    private val linkRepo: LinkRepository,
    private val noteRepo: NoteRepository,
    private val locationSearch: LocationSearchService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Entity Count Statistics
     */
    fun entityStats(): Map<String, Long> {
        val stat: MutableMap<String, Long> = mutableMapOf()
        stat[EntityType.Dish.path] = dishRepo.itemCount()
        stat[EntityType.Feed.path] = linkRepo.feedCount()
        stat[EntityType.Note.path] = noteRepo.itemCount()
        for (et in listOf(EntityType.Post,EntityType.Tour,EntityType.Place,EntityType.Video,EntityType.Photo)) {
            stat[et.path] = locationSearch.visibleItemCount(et)
        }
        stat["pois"] = locationSearch.visibleItemsWithCoordinatesCount()
        this.log.debug("[Metrics] Current Stats: {}", stat)
        return stat
    }

}
