package net.timafe.angkor.domain

import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.*
import javax.persistence.*

/*
{
 "origin": "gr",
 "lastServed": "2015-07-07T21:33:37.718Z",
 "authenticName": "ελληνική σαλάτα",
 "timesServed": 5,
 "rating": 9,
 "createdAt": "2015-08-09-09T22:22:22.222Z",
 "primaryUrl": "http://de.allrecipes.com/rezept/1268/authentischer-griechischer-salat.aspx",
 "updatedBy": "kuhnibaert@gmail.com",
 "name": "Griechischer Salat",
 "imageUrl": "http://www.casualcatering.com/shop/images/greek.jpg",
 "updatedAt": "2018-09-19T21:58:30.910Z",
 "notes": "1. Paprikaschoten halbieren, entkernen und in 2 cm große Würfel schneiden. Tomaten sechsteln. Salatgurke längs vierteln und quer in 2 cm große Stücke schneiden. Zwiebeln in 1 cm dicke Scheiben schneiden. Minze in feine Streifen schneiden. Oliven halbieren, Schafskäse in 2 cm große Würfel schneiden.\n\n2. Essig mit 10 El kaltem Wasser, Öl, Salz und Pfeffer in einer Schüssel verrühren. Paprikaschoten, Tomaten, Gurke, Zwiebeln, Minze, Schafskäse und Oliven mit dem Dressing mischen und kurz durchziehen lassen. Dazu passt Fladenbrot.",
 "id": "5585e234e4b062eca3674e08",
 "tags": [
     "feta",
     "oliven",
     "salat",
     "tomaten",
     "veggy"
 ]
}*/
@Entity
@TypeDef(
        name = "list-array",
        typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
data class Dish(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: UUID?,

        var name: String,
        var areaCode: String,
        var summary: String?,
        var imageUrl: String?,
        var primaryUrl: String?,

        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "scope")
        @Type(type = "pgsql_enum")
        var authScope: AuthScope = AuthScope.PUBLIC,

        @Type(type = "list-array")
        @Column(
                name = "tags",
                columnDefinition = "text[]"
        )
        var tags: List<String> = listOf()
        //var updated: LocalDateTime = LocalDateTime
        //
        //.now()
)

