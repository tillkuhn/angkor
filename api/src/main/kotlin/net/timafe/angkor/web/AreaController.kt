package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.service.AreaService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class AreaController(
    private val areaService: AreaService
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/areas")
    @ResponseStatus(HttpStatus.OK)
    // https://www.baeldung.com/spring-data-sorting#1-sorting-with-the-orderby-method-keyword
    fun allAreas(): List<Area> {
        return areaService.allAreas()
    }

    @PostMapping("/areas")
    @ResponseStatus(HttpStatus.CREATED)
    fun createArea(@RequestBody item: Area): ResponseEntity<Area> {
        log.debug("Post area $item")
        val saveItem: Area = areaService.save(item)
        return ResponseEntity.ok().body(saveItem)
    }

    @GetMapping
    @RequestMapping("/area-tree")
    fun areaTree(): List<TreeNode> = areaService.getAreaTree()

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/countries")
    fun countriesAndRegions(): List<Area> {
        return areaService.countriesAndRegions()
    }

    @DeleteMapping("{id}")
    fun deleteArea(@PathVariable(value = "id") code: String): ResponseEntity<Void> {
        return areaService.findOne(code).map {
            areaService.delete(code)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }
}
