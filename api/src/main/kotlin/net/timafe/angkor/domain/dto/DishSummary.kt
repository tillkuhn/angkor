package net.timafe.angkor.domain.dto

import net.timafe.angkor.domain.enums.AuthScope
import org.springframework.beans.factory.annotation.Value
import java.util.*

/**
 * Projection Interface
 * https://www.baeldung.com/spring-data-jpa-projections
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
 */
interface DishSummary {
    // Satisfy entity query in PlaceRepository which cannot cast coorindates arg
    //constructor(id: UUID, name: String, summary: String, areaCode: String, primaryUrl: String?, locationType: LocationType, coordinates: Any) : this(id, name, summary, areaCode, primaryUrl, locationType, coordinates as List<Double>, listOf())
    var id: UUID
    var name: String
    var summary: String?
    var areaCode: String?
    var primaryUrl: String?
    var authScope: AuthScope
    var updatedAt: String?

    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
    // Target is of type input: MutableMap<String, Any> (
    @Value("#{@mappingService.postgresArrayStringToList(target.tags)}")
    fun getTags(): List<String>

}
