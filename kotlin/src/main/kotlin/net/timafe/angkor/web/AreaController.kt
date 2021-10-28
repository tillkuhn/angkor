package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.service.AreaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/areas")
class AreaController(
    private val service: AreaService
) : AbstractEntityController<Area, Area, String>(service) {

    override fun mergeUpdates(currentItem: Area, newItem: Area): Area =
        currentItem
            .copy(
                name = newItem.name,
                parentCode = newItem.parentCode,
                level = newItem.level,
                adjectival = newItem.adjectival
            )


    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping
    fun findAll(): List<Area> = service.findAll()

    // @Deprecated("/area should support tree=true queryParam to distinguish flat / tree result")
    @GetMapping
    @RequestMapping("/tree")  // translates to areas/tree
    fun areaTree(): List<TreeNode> = service.getAreaTree()

    @Deprecated("Should get /area and filter on client side")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/countries") // translates to areas/countries
    fun countriesAndRegions(): List<Area> {
        return service.countriesAndRegions()
    }
}
