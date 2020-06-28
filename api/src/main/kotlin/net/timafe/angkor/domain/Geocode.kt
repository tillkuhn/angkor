package net.timafe.angkor.domain

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@Entity
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType::class
        )
data class Geocode(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        var code: String?,

        var name: String,
        var parentCode: String,

        // https://vladmihalcea.com/the-best-way-to-map-an-enum-type-with-jpa-and-hibernate/
        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "level")
        @Type( type = "pgsql_enum" )
        var   level: GeocodeLevel = GeocodeLevel.COUNTRY

)

