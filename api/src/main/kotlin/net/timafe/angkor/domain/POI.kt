package net.timafe.angkor.domain

import java.util.*

data class POI (

        var id: UUID? = null,
        var name: String? = null,

        // coordinates should be List<Double>? but this didn't work with JPA SELECT NEW query
        // (see PlaceRepository) which raises
        // Expected arguments are: java.util.UUID, java.lang.String, java.lang.Object
        var coordinates: java.lang.Object? = null


)
