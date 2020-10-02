package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.LocationType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

/**
 * {
"id": "70c4916f-e621-477b-8654-44952491bee1",
"name": "Sperlonga",
"country": "it",
"imageUrl": "https://www.portanapoli.de/sites/default/files/styles/half_column_250/public/pictures/taxonomy/sperlonga_by_night.jpg?itok=uCh02nl8",
"lotype": "BEACH",
"coordinates": [
13.42714,
41.26367
],
"primaryUrl": "https://www.portanapoli.de/sperlonga",
"summary": "Tip Sperlonga mit herrlichen Sandstränden und einer malerischen Altstadt ist einer der schönsten Orte Süditaliens.",
"notes": "Sperlonga ist einer der malerischsten Orte Süditaliens.\n Bezaubernd ist der Blick von der Altstadt.",
"createdAt": "2019-10-02T18:57:27.534Z",
"createdBy": "test@test.de",
"updatedAt": "2019-11-09T12:15:45.689Z",
"updatedBy": "test@test.de"
}
 */
@Entity
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

        // audit
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var createdAt: LocalDateTime? = LocalDateTime.now(),
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
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

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updatedAt = LocalDateTime.now();
    }

}

