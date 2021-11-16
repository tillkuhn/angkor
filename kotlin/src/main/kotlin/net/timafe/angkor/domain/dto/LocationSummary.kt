package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import java.time.ZonedDateTime
import java.util.*

/**
 * Projection DTO for location searches with slim result summaries
 *
 * Constructor args:
 * listOf("areaCode","authScope","id","imageUrl","name","primaryUrl","updatedAt","updatedBy","coordinates","tags","type")
 */
data class LocationSummary(

    // "areaCode","authScope","id","imageUrl","name","primaryUrl","updatedAt","updatedBy","coordinates","tags","type"
    // Public Properties
    val areaCode: String?,
    val authScope: AuthScope,
    val id: UUID,
    val imageUrl: String?,
    val name: String,
    val primaryUrl: String?,
    val updatedAt: ZonedDateTime?,
    val updatedBy: UUID?,
    // Private "backing" Properties
    @JsonIgnore val _coordinates: Any, // List<Double>, // Object
    @JsonIgnore val _tags: Any, // List<String>, // Object
    @JsonIgnore val _entityClass: Class<Any>, // returned by type() in Criteria API

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

    val entityType: String /*EntityType*/
        get() {
            return EntityType.fromEntityClass(_entityClass).titlecase()
        }

    // lateinit var entityType: EntityType // derived from class in secondary constructor
    // expose the Concrete class (useful for UI)
    // @JsonProperty
    // fun previewUrl() = "hase"

    // we need a custom constructor here since Criteria API apparently can't handle list properties
    // properly (Unable to locate appropriate constructor on class ), using Object instead of List<>
    // also we get the type as a Java class object, but want to return the Entity
    //    @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
    //    constructor(
    //        areaCode: String?, authScope: AuthScope, coordinates: Any, id: UUID, imageUrl: String?, name: String, primaryUrl: String?,
    //        tags: Any, updatedAt: ZonedDateTime?, updatedBy: UUID?, entityClass: Class<Any>
    //    ) : this(
    //        areaCode, authScope, coordinates as List<Double>, id, imageUrl, name, primaryUrl,
    //        tags as List<String>, updatedAt, updatedBy
    //    ) {
    //        this.entityType = EntityType.fromEntityClass(entityClass)
    //    }
}

