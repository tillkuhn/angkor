package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.POI
import net.timafe.angkor.repo.PlaceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION )
class MapController {

    private val log = LoggerFactory.getLogger(javaClass)
    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @GetMapping("/pois")
    fun getCoordinates(): List<POI> {
        log.debug("REST request to get all coordinates")
        //val list = mutableListOf<POI>()

        /*
        val mapper =  DynamoDBMapper(amazonDynamoDB)
        val nameMap = HashMap<String, String>()
        nameMap["#name"] = "name" // reserved keyword
        val scanExpression =  DynamoDBScanExpression().withExpressionAttributeNames(nameMap).withProjectionExpression("id,coordinates,country,#name")
        val iList = mapper.scan(Place::class.java,scanExpression)
        val iter = iList.iterator()
        while (iter.hasNext()) {
            val item = iter.next();
            list.add(Coordinates(item.id,item.name,item.coordinates))
            //log.debug("hase"+item)
        }
         */
        return placeRepository.findPointOfInterests()
    }
}
