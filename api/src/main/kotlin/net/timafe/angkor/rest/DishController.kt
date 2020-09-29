package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.repo.DishRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/dishes")
class DishController {

    @Autowired
    private lateinit var dishRepository: DishRepository

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun allDishes(principal: Principal?): List<Dish> {
        // val dishes = if (principal != null)  placeRepository.findByOrderByName() else placeRepository.findPublicPlaces()
        val dishes = dishRepository.findAll()
        //  coo ${places.get(0).coordinates}"
        log.info("allDishes() return ${dishes.size} dishes principal=${principal}")
        return dishes
    }

    @GetMapping("scoped")
    @ResponseStatus(HttpStatus.OK)
    fun allDishesByAuthScope(): List<Dish> {
        val authScopes = listOf(AuthScope.PUBLIC)
        val dishes = dishRepository.findDishesByAuthScope(authScopes)
        log.info("findDishesByAuthScope() return ${dishes.size} dishes authScopes=${authScopes}")
        return dishes
    }

    @GetMapping("search/{query}")
    @ResponseStatus(HttpStatus.OK)
    fun allDishesByQuery(@PathVariable query: String): List<Dish> {
        val authScopes = listOf(AuthScope.PUBLIC)
        val dishes = dishRepository.findDishesByQuery(query)
        log.info("findDishesByQuery(${query}) return ${dishes.size} dishes authScopes=${authScopes}")
        return dishes
    }


}
