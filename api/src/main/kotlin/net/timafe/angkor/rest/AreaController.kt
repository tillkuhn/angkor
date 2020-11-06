package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.repo.AreaRepository
import net.timafe.angkor.service.AreaService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * CHeck out
 * https://www.callicoder.com/kotlin-spring-boot-mysql-jpa-hibernate-rest-api-tutorial/
 */
@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION)
class AreaController(
        private val areaRepository: AreaRepository,
        private val areaService: AreaService
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/areas")
    @ResponseStatus(HttpStatus.OK)
    // https://www.baeldung.com/spring-data-sorting#1-sorting-with-the-orderby-method-keyword
    fun areacodes(): List<Area> {
        return areaRepository.findByOrderByName()
    }

    @PostMapping("/areas")
    @ResponseStatus(HttpStatus.CREATED)
    fun createArea(@RequestBody item: Area): Area = areaRepository.save(item)

    @GetMapping
    @RequestMapping("/area-tree")
    fun areaTree(): List<TreeNode>  = areaService.getAreaTree()

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/countries")
    fun countriesAndRegions(): List<Area> {
        // return areaRepository.findByLevelOrderByName(AreaLevel.COUNTRY)
        return areaRepository.findAllAcountiesAndregions()
    }

}
