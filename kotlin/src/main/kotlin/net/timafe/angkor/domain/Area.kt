package net.timafe.angkor.domain

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.interfaces.Mappable
import org.hibernate.annotations.Type
import java.util.*
import jakarta.persistence.*

/**
 * Area code (Managed Domain Entity)
 *
 * represents a code for an area (as opposed to Points),
 * most importantly countries, but also continents or regions
 */
@Entity
data class Area(

    @Id
    var code: String,

    var name: String,
    var parentCode: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "level")
    @Type(PostgreSQLEnumType::class)
    var level: AreaLevel = AreaLevel.COUNTRY,

    /**
     * Adjectival representation of the name, e.g. France => French
     */
    var adjectival: String? = null,

    @Type(ListArrayType::class)
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf(), /* lon, lat */

) : Mappable {

    /**
     * Convert country code to emoji code flag
     *
     * - https://gist.github.com/bhurling/c955c778f7a0765aaffd9214b12b3963
     * - https://dev.to/jorik/country-code-to-flag-emoji-a21
     */
    val emoji: String?
        get() {
            if (!code.matches("^[a-zA-Z]{2}$".toRegex())) {
                return null // not a country code
            }
            return code
                .uppercase(Locale.US)
                .map { char ->
                    Character.codePointAt("$char", 0) - 0x41 + 0x1F1E6
                }
                .map { codePoint ->
                    Character.toChars(codePoint)
                }
                .joinToString(separator = "") { charArray ->
                    String(charArray)
                }
        }
}


