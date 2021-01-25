package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.rest.vm.NumberResult
import net.timafe.angkor.security.AuthService
import net.timafe.angkor.security.SecurityUtils
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
    private val authService: AuthService,
    private val repo: DishRepository
): ResourceController<Dish,DishSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getItem(@PathVariable id: UUID): ResponseEntity<Dish> {
        return repo.findById(id).map { item ->
            if (SecurityUtils.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }.orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("{id}")
    override fun deleteItem(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting item id=$id")
        return repo.findById(id).map { item ->
            repo.delete(item)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createItem(@RequestBody item: Dish): Dish = repo.save(item)

    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun updateItem(@Valid @RequestBody newItem: Dish, @PathVariable id: UUID): ResponseEntity<Dish> {
        log.info("update () called for item $id")
        return repo.findById(id).map { currentItem ->
            val updatedItem: Dish = currentItem
                    .copy(name = newItem.name,
                            summary = newItem.summary,
                            notes = newItem.notes,
                            areaCode = newItem.areaCode,
                            primaryUrl = newItem.primaryUrl,
                            imageUrl = newItem.imageUrl,
                            authScope = newItem.authScope,
                            tags = newItem.tags
                    )
            ResponseEntity.ok().body(repo.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }


    /**
     * increase times served counter by one
     */
    @PutMapping(value = ["{id}/just-served"])
    fun justServed(@PathVariable id: UUID): ResponseEntity<NumberResult> {
        val dish = repo.findById(id)
        return if (dish.isPresent) {

            dish.get().timesServed = dish.get().timesServed.inc()
            val newCount = dish.get().timesServed
            log.info("New timesServed Count $newCount")
            repo.save(dish.get())
            // ResponseEntity.ok().body(BooleanResult(true))
            ResponseEntity.ok().body(NumberResult(newCount.toInt()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

//    @GetMapping
//    override fun getAll(): List<DishSummary> {
//        return searchAll()
//    }

    @GetMapping("search/")
    fun searchAll(): List<DishSummary> {
        return search("")
    }

    @GetMapping("search/{search}")
    override fun search(@PathVariable(required = false) search: String): List<DishSummary> {
        val authScopes = SecurityUtils.allowedAuthScopesAsString()
        val dishes = repo.search(search, authScopes)
        log.info("allDishesBySearch(${search}) return ${dishes.size} dishes authScopes=${authScopes}")
        return dishes
    }


}
