package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.repo.EventRepository
import net.timafe.angkor.rest.vm.NumberResult
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.DishService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping(Constants.API_LATEST + "/dishes")
class DishController(
    private val service: DishService,
    private val eventRepo: EventRepository
) : ResourceController<Dish, DishSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun findOne(@PathVariable id: UUID): ResponseEntity<Dish> {
        return service.findOne(id).map { item ->
            if (SecurityUtils.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build()
        }.orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("{id}")
    override fun delete(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        return service.findOne(id).map {
            service.delete(id)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@RequestBody item: Dish): Dish = service.save(item)

    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun save(@Valid @RequestBody newItem: Dish, @PathVariable id: UUID): ResponseEntity<Dish> {
        return service.findOne(id).map { currentItem ->
            val updatedItem: Dish = currentItem
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
            ResponseEntity.ok().body(service.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }


    /**
     * increase times served counter by one
     */
    @PutMapping(value = ["{id}/just-served"])
    fun justServed(@PathVariable id: UUID): ResponseEntity<NumberResult> {
        val dish = service.findOne(id)
        return if (dish.isPresent) {

            dish.get().timesServed = dish.get().timesServed.inc()
            val newCount = dish.get().timesServed
            log.info("New timesServed Count $newCount")
            service.save(dish.get())
            // ResponseEntity.ok().body(BooleanResult(true))
            // new: record this as an event
            val servedEvent = Event(
                entityType = net.timafe.angkor.domain.enums.EntityType.DISH,
                entityId = dish.get().id,
                eventType = net.timafe.angkor.domain.enums.EventType.DISH_SERVED,
                summary = "Dish ${dish.get().name} just served"
            )
            eventRepo.save(servedEvent)
            ResponseEntity.ok().body(NumberResult(newCount.toInt()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Deprecated, use new POST API
     */
    @GetMapping("search/{search}")
    fun searchDeprecated(@PathVariable(required = false) search: String): List<DishSummary> {
        return service.search(SearchRequest(search))
    }

    /**
     * Search all items
     */
    @GetMapping("search/")
    fun searchAll(): List<DishSummary> {
        return search(SearchRequest()) // Search with default request (empty string)
    }

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    override fun search(@Valid @RequestBody search: SearchRequest): List<DishSummary> = service.search(search)

}
