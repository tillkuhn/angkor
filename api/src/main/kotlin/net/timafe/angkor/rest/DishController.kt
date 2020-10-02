package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.service.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/dishes")
class DishController(
        private val authService: AuthService,
        private val repo: DishRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(principal: Principal?): List<Dish> {
        val dishes = repo.findAll()
        log.info("allDishes() return ${dishes.size} dishes principal=${principal}")
        return dishes
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getItem(@PathVariable id: UUID): ResponseEntity<Dish> {
        return repo.findById(id).map { item ->
            // Todo check if viewable
            // if (item.areaCode == "th") ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            if (authService.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }.orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("{id}")
    fun deleteItem(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting item id=$id")
        return repo.findById(id).map { item ->
            repo.delete(item)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNew(@RequestBody item: Dish): Dish = repo.save(item)

    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    fun updateItem(@Valid @RequestBody newItem: Place, @PathVariable id: UUID): ResponseEntity<Dish> {
        log.info("update () called for item $id")
        return repo.findById(id).map { currentItem ->
            val updatedItem: Dish = currentItem
                    .copy(name = newItem.name,
                            summary = newItem.summary,
                            notes = newItem.notes,
                            areaCode = newItem.areaCode,
                            primaryUrl = newItem.primaryUrl,
                            imageUrl = newItem.imageUrl,
                            authScope = newItem.authScope
                    )
            ResponseEntity.ok().body(repo.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("search/")
    fun searchAll(): List<DishSummary> {
        return search("")
    }

    @GetMapping("search/{search}")
    fun search(@PathVariable(required = false) search: String?): List<DishSummary> {
        val authScopes = authService.allowedAuthScopesAsString()
        val dishes = repo.search(search, authScopes)
        log.info("allDishesBySearch(${search}) return ${dishes.size} dishes authScopes=${authScopes}")
        return dishes
    }


}
