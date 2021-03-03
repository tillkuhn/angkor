package net.timafe.angkor.service

import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.repo.PlaceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StatService(
    private val placeRepo: PlaceRepository,
    private val dishRepo: DishRepository,
    private val noteRepo: NoteRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Entity Count Statistics
     */
    fun entityStats(): Map<String,Long>  {
        val stat: MutableMap<String,Long> = mutableMapOf();
        stat.put(EntityType.PLACE.path,placeRepo.itemCount())
        stat.put("pois",placeRepo.itemCount()) // should be separate count with e.g. countrie POIs on top
        stat.put(EntityType.NOTE.path,noteRepo.itemCount())
        stat.put(EntityType.DISH.path,dishRepo.itemCount())
        this.log.debug("Current Stats: $stat")
        return stat
    }

}
