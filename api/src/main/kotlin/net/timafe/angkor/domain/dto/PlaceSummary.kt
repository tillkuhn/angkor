package net.timafe.angkor.domain.dto

import net.timafe.angkor.domain.Mappable
import net.timafe.angkor.domain.Taggable
import net.timafe.angkor.domain.enums.LocationType
import java.util.*

data class PlaceSummary(

        var id: UUID,
        var name: String,
        var summary: String?,
        var areaCode: String,
        var primaryUrl: String?,
        var locationType: LocationType = LocationType.PLACE,
        // coordinates should be List<Double>? but this didn't work with JPA SELECT NEW query
        // (see PlaceRepository) which raises
        // Expected arguments are: java.util.UUID, java.lang.String, java.lang.Object
        // var coordinates: java.lang.Object? = null
        override var coordinates: List<Double> = listOf(),
        override var tags: List<String> = listOf()

) : Mappable, Taggable {
    // Satisfy entity query in PlaceRepository which cannot cast coorindates arg
    constructor(id: UUID, name: String, summary: String, areaCode: String, primaryUrl: String?, locationType: LocationType, coordinates: Any) : this(id, name, summary, areaCode, primaryUrl, locationType, coordinates as List<Double>, listOf())
}

