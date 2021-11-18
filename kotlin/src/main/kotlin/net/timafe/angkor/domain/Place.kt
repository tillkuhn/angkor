package net.timafe.angkor.domain

import net.timafe.angkor.domain.enums.LocationType
import org.hibernate.annotations.Type
import java.time.LocalDate
import javax.persistence.*

/**
 * Entity that represents a Place
 */
@Entity
@DiscriminatorValue("Place")
class Place(

    var summary: String? = null,
    var notes: String?,
    var beenThere: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "ltype")
    @Type(type = "pgsql_enum")
    var locationType: LocationType = LocationType.PLACE,

    ) : Location()

//@Entity
//@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
//@TypeDef(
//    name = "list-array",
//    typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
//)
//@ManagedEntity(entityType = EntityType.Place)
//data class Place (
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    override var id: UUID?,
//
//    var name: String,
//    var areaCode: String,
//    var summary: String?,
//    var notes: String?,
//    var imageUrl: String?,
//    var primaryUrl: String?,
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
//    var lastVisited: LocalDate?,
//
//    // Audit Fields
//    @CreatedDate
//    var createdAt: ZonedDateTime? = ZonedDateTime.now(),
//
//    @CreatedBy
//    var createdBy: UUID?,
//
//    @LastModifiedDate
//    var updatedAt: ZonedDateTime? = ZonedDateTime.now(),
//
//    @LastModifiedBy
//    var updatedBy: UUID?,
//
//    @Enumerated(EnumType.STRING)
//    @Column(columnDefinition = "location_type")
//    @Type(type = "pgsql_enum")
//    var locationType: LocationType = LocationType.PLACE,
//
//    @Enumerated(EnumType.STRING)
//    @Column(columnDefinition = "scope")
//    @Type(type = "pgsql_enum")
//    override var authScope: AuthScope = AuthScope.PUBLIC,
//
//    @Type(type = "list-array")
//    @Column(
//        name = "coordinates",
//        columnDefinition = "double precision[]"
//    )
//    override var coordinates: List<Double> = listOf() /* 0.0, 0.0 */,
//
//    @Type(type = "list-array")
//    @Column(
//        name = "tags",
//        columnDefinition = "text[]"
//    )
//    override var tags: MutableList<String> = mutableListOf()
//
//) : Mappable, Taggable, EventSupport {
//
//    override fun description(): String {
//        return "${this.name} (${this.areaCode})"
//    }
//
//    // Overwrite toString() to make it less verbose
//    // Example: Place(id=81d06f34-99ed-421e-b33c-3d377e665eb6, name=Some Beach (Java)
//    override fun toString() = "Place(id=${this.id}, name=${this.name})"
