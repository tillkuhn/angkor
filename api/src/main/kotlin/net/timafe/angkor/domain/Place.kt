package net.timafe.angkor.domain

import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
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
        var country: String,
        var summary: String?,
        var imageUrl: String?,
        var primaryUrl: String?,

        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "lotype")
        @Type(type = "pgsql_enum")
        var lotype: LocationType = LocationType.PLACE,

        @Type(type = "list-array")
        @Column(
                name = "coordinates",
                columnDefinition = "double precision[]"
        )
        var coordinates: List<Double> = listOf(0.0, 0.0)
        //@DynamoDBAttribute
        //var updated: LocalDateTime = LocalDateTime.now()
)

