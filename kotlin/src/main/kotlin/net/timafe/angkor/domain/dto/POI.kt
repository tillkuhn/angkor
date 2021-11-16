package net.timafe.angkor.domain.dto

import org.springframework.beans.factory.annotation.Value
import java.util.*

/**
 * Lightweight Point Of Interest representation,
 * basically coordinates with a name and area attached
 */
interface POI {
    // ("id","name","areaCode","imageUrl","locationType","coordinates")
    var id: UUID
    var name: String
    var areaCode: String
    var imageUrl: String?
    var locationType: String?

    // coordinates should be List<Double>? but this didn't work with JPA SELECT NEW query
    // (see PlaceRepository) which raises
    // Expected arguments are: java.util.UUID, java.lang.String, java.lang.Object
    // var coordinates: java.lang.Object? = null
    // override var coordinates: List<Double> = listOf()
    @Value("#{@mappingService.postgresCoordinateStringToList(target.coordinates)}")
    fun getCoordinates(): List<Double>

}
