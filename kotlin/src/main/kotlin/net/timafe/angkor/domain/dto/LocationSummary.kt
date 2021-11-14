package net.timafe.angkor.domain.dto

import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import java.time.ZonedDateTime
import java.util.*

/**
 * Projection DTO for location searches
 *
 * Constructor args:
 * listOf("areaCode","authScope","coordinates","entityType","id","imageUrl","name","primaryUrl","tags","updatedAt"."updatedBy")
 */
data class LocationSummary(
    // val createdAt: ZonedDateTime = ZonedDateTime.now(),
    // val createdBy: UUID = UUID.fromString(Constants.USER_SYSTEM),
    // val externalId: String? = null,
    // val properties: MutableMap<String, String> = mutableMapOf(),
    //  al geoAddress: String? = null,
    // val version: Long = 0,

    val areaCode: String?,
    val authScope: AuthScope,
    val coordinates: List<Double>, // Object
    val id: UUID,
    val imageUrl: String?,
    val name: String,
    val primaryUrl: String?,
    val tags: List<String>, // Object
    val updatedAt: ZonedDateTime?,
    val updatedBy: UUID?,
) {
    lateinit var entityType: EntityType // derived from class in secondary constructor

    // expose the Concrete class (useful for UI)
    // @JsonProperty
    // fun previewUrl() = "hase"

    // we need a custom constructor here since Criteria API apparently can't handle list properties
    // properly (Unable to locate appropriate constructor on class ), using Object instead of List<>
    // also we get the type as a Java class object, but want to return the Entity
    @Suppress("UNCHECKED_CAST") // See explanation above why we need to suppress the warning here
    constructor(
        areaCode: String?,
        authScope: AuthScope, coordinates: Any, id: UUID, imageUrl: String?, name: String, primaryUrl: String?,
        tags: Any, updatedAt: ZonedDateTime?, updatedBy: UUID?, entityClass: Class<Any>
    ) : this(
        areaCode, authScope, coordinates as List<Double>, id, imageUrl, name, primaryUrl,
        tags as List<String>, updatedAt, updatedBy
    ) {
        // todo we should adapt EntityType members to titlecase so they have the same name as SimpleClass
        this.entityType = EntityType.valueOf(entityClass.simpleName.uppercase())
    }
}

