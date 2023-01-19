package net.timafe.angkor.domain

import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.enums.LocationType
import org.hibernate.annotations.Type
import java.time.LocalDate
import jakarta.persistence.*

/**
 * Place 2 Go (Managed Domain Entity)
 * A special kind of location
 */
@Entity
@DiscriminatorValue("Place")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = true, eventOnDelete = true)
class Place(

    var summary: String? = null,
    var notes: String?,
    var beenThere: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "ltype")
    @Type(type = "pgsql_enum")
    var locationType: LocationType = LocationType.PLACE,

    ) : LocatableEntity()
