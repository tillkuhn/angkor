package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.config.annotations.ManagedEntity
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LocationType
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.domain.interfaces.Mappable
import net.timafe.angkor.domain.interfaces.Taggable
import net.timafe.angkor.service.EntityEventListener
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@TypeDef(
    name = "list-array",
    typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
@ManagedEntity(entityType = EntityType.PLACE)
data class Place(

    // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: UUID?,

    var name: String,
    var areaCode: String,
    var summary: String?,
    var notes: String?,
    var imageUrl: String?,
    var primaryUrl: String?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
    var lastVisited: LocalDate?,

    // Audit Fields
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: LocalDateTime? = LocalDateTime.now(),

    @CreatedBy
    var createdBy: UUID?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @LastModifiedDate
    var updatedAt: LocalDateTime? = LocalDateTime.now(),

    @LastModifiedBy
    var updatedBy: UUID?,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "location_type")
    @Type(type = "pgsql_enum")
    var locationType: LocationType = LocationType.PLACE,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    @Type(type = "pgsql_enum")
    override var authScope: AuthScope = AuthScope.PUBLIC,

    @Type(type = "list-array")
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf() /* 0.0, 0.0 */,

    @Type(type = "list-array")
    @Column(
        name = "tags",
        columnDefinition = "text[]"
    )
    override var tags: MutableList<String> = mutableListOf()

) : Mappable, Taggable, EventSupport {

    override fun description(): String {
        return "${this.name} (${this.areaCode})"
    }

    // Overwrite toString() to make it less verbose
    // Example: Place(id=81d06f34-99ed-421e-b33c-3d377e665eb6, name=Some Beach (Java)
    override fun toString() = "Place(id=${this.id}, name=${this.name})"
}

