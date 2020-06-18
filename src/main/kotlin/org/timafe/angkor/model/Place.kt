package org.timafe.angkor.model

import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.*
import javax.persistence.*

@Entity
@TypeDef(
        name = "list-array",
        typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
// @DynamoDBTable(tableName = Constants.DYNAMODB_PREFIX + "place")
data class Place(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: UUID?,

        var name: String,
        var country: String,

        var summary: String?,

        @Type(type = "list-array")
        @Column(
                name = "coordinates",
                columnDefinition = "double precision[]"
        )
        var coordinates: List<Double> = listOf(1.2, 3.4)
        //@DynamoDBAttribute
        //var updated: LocalDateTime = LocalDateTime.now()
)

