package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import net.timafe.angkor.domain.enums.EntityType
import org.springframework.beans.factory.annotation.Value
import java.util.*

/**
 * A classed based Projection DTO for Point representations of a Location to be displayed on a map
 *
 * Contains: mainly coordinates with name and area, and the entity type to support detail URLs
 *
 * Typically, it is used in JPA Queries / Criteria Queries with "select new()"
 *
 * We use backing fields, so we use the default constructor that fits the needs
 * of the JPA, using  an underscore prefix to distinguish it from the actual
 * public property that provides the custom logic to return the Type
 * (or transformed value) we want to expose.
 *
 * [@JsonIgnore] is required to hide such backing fields from being exposed by ObjectMapper
 *
 * CAUTION: If you change the order of properties, you need to make sure
 * that the Constructor logic in JPA queries still fits!
 */
data class LocationPOI(

    val areaCode: String?,
    // BackingField for Coordinates List which is returned as Object in Java by JPA Criteria API
    @JsonIgnore val _coordinates: Any, // Any == Object (Java)
    val id: UUID,
    val imageUrl: String?,
    val name: String,
    // BackingField for EntityType as we get the Class from JPA Criteria API, and compute it in getter below
    @JsonIgnore val _entityClass: Class<Any>, // returned by type() in Criteria API

    ) {

    val coordinates: List<Double>
        get() {
            @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
            return _coordinates as List<Double>
        }

    val entityType: EntityType
        get() {
            return EntityType.fromEntityClass(_entityClass)
        }

    // Alternative "SPeL approach", used for Interfaces to Inject none-compatible Objects
    // @Value("#{@mappingService.postgresCoordinateStringToList(target.coordinates)}")
    // fun getCoordinates(): List<Double>
}
