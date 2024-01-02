package net.timafe.angkor.domain

// import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import jakarta.persistence.*
import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.enums.LocationType
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import java.time.LocalDate

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
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var locationType: LocationType = LocationType.PLACE,

    ) : LocatableEntity()
