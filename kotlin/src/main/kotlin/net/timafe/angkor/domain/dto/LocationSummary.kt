package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import java.time.ZonedDateTime
import java.util.*

/**
 * A class based Projection DTO for location searches with slim result summaries
 *
 * See https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections.dtos
 *
 * Constructor args:
 * listOf("areaCode","authScope","id","imageUrl","name","primaryUrl","updatedAt","updatedBy","coordinates","tags","type")
 */
data class LocationSummary(

    // Public Properties
    val areaCode: String?,
    //FIXME Hibernate 6 can't find Instantiation strategy for AuthScope probably since it's an enum
    //  Unable to determine dynamic instantiation injection strategy for net.timafe.angkor.domain.dto.LocationSummary#authScope
    // val authScope: AuthScope,
    val id: UUID,
    val imageUrl: String?,
    val name: String,
    val primaryUrl: String?,
    val updatedAt: ZonedDateTime?,
    val updatedBy: UUID?,

    // Private "backing" Properties
    @JsonIgnore val _coordinates: Any, // List<Double>,
    @JsonIgnore val _tags: Any, // List<String>,
    @JsonIgnore val _entityClass: Class<Any>, // returned by type() in Criteria API
    @JsonIgnore val _authScope: Any, //  AuthScope

) {
    val coordinates: List<Double>
        get() {
            @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
            return _coordinates as List<Double>
        }
    val tags: List<String>
        get() {
            @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
            return _tags as List<String>
        }

    val entityType: EntityType
        get() {
            return EntityType.fromEntityClass(_entityClass)
        }
    val authScope: AuthScope
        get() {
            @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
            return _authScope as AuthScope
        }

}

