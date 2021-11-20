package net.timafe.angkor.domain

import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LocationType
import net.timafe.angkor.service.EntityEventListener
import org.hibernate.annotations.Type
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import javax.persistence.*

/**
 * Place 2 Go (Managed Domain Entity)
 * A special kind of location
 */
@Entity
@DiscriminatorValue("Place")
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@EntityTypeInfo(type = EntityType.Place, eventOnCreate = true, eventOnUpdate = true, eventOnDelete = true)
class Place(

    var summary: String? = null,
    var notes: String?,
    var beenThere: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "ltype")
    @Type(type = "pgsql_enum")
    var locationType: LocationType = LocationType.PLACE,

    ) : LocatableEntity()
