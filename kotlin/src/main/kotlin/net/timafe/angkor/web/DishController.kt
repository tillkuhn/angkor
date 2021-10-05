package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.service.DishService
import net.timafe.angkor.web.vm.NumberResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(Constants.API_LATEST + "/dishes")
class DishController(
    private val service: DishService,
) : AbstractEntityController<Dish, DishSummary, UUID>(service) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun mergeUpdates(currentItem: Dish, newItem: Dish): Dish =
        currentItem
            .copy(
                name = newItem.name,
                summary = newItem.summary,
                notes = newItem.notes,
                areaCode = newItem.areaCode,
                primaryUrl = newItem.primaryUrl,
                imageUrl = newItem.imageUrl,
                authScope = newItem.authScope,
                tags = newItem.tags,
                rating = newItem.rating
            )

    /**
     * increase times served counter by one
     */
    @PutMapping(value = ["{id}/just-served"])
    fun justServed(@PathVariable id: UUID): ResponseEntity<NumberResult> {
        val dish = service.findOne(id)
        return if (dish.isPresent) {
            val newCount = service.justServed(dish.get())
            ResponseEntity.ok().body(NumberResult(newCount))
        } else {
            ResponseEntity.notFound().build()
        }
    }


}
