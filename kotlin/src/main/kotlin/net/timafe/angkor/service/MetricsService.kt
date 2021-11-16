package net.timafe.angkor.service

import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MetricsService(
    private val dishRepo: DishRepository,
    private val linkRepo: LinkRepository,
    private val noteRepo: NoteRepository,
    private val placeRepo: PlaceRepository,
    private val postRepo: PostRepository,
    private val tourRepo: TourRepository,
    private val videoRepo: VideoRepository,
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
        stat[EntityType.Place.path] = placeRepo.itemCount()
        stat[EntityType.Post.path] = postRepo.itemCount()
        stat[EntityType.Tour.path] = tourRepo.itemCount()
        stat[EntityType.Video.path] = videoRepo.itemCount()
        // should be separate count with e.g. countries POIs on top
        stat["pois"] = placeRepo.itemsWithCoordinatesCount()
        this.log.debug("[Metrics] Current Stats: $stat")
        return stat
    }

}
