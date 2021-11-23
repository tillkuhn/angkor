package net.timafe.angkor.domain

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.interfaces.Mappable
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

/**
 * Area code (Managed Domain Entity)
 *
 * represents a code for an area (as opposed to Points),
 * most importantly countries, but also continents or regions
 */
@Entity
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
data class Area(

    @Id
    var code: String,

    var name: String,
    var parentCode: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "level")
    @Type(type = "pgsql_enum")
    var level: AreaLevel = AreaLevel.COUNTRY,

    /**
     * Adjectival representation of the name, e.g. France => French
     */
    var adjectival: String? = null,

    @Type(type = "list-array")
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf() /* lon, lat */,

    var emoji: String = "\uD83C\uDF10"

): Mappable


