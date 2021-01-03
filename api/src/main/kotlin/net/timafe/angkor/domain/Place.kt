package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.LocationType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@TypeDef(
        name = "list-array",
        typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
data class Place(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: UUID?,

        var name: String,
        var areaCode: String,
        var summary: String?,
        var notes: String?,
        var imageUrl: String?,
        var primaryUrl: String?,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var lastVisited: LocalDate? = LocalDate.now(),

        // audit
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
        @CreatedDate
        var createdAt: LocalDateTime? = LocalDateTime.now(),

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
        @LastModifiedDate
        var updatedAt: LocalDateTime? = LocalDateTime.now(),

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
        override var tags: List<String> = listOf()

) : Mappable, Taggable, AuthScoped {

    /*
    @PrePersist
    @PreUpdate
    fun prePersist() {
        updatedAt = LocalDateTime.now();
    }
     */

}

