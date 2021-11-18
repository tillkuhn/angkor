package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.config.annotations.ManagedEntity
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.service.EntityEventListener
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.EntityListeners

/**
 * Entity that represents a Tour,
 * typically backed by an external Komoot URL
 */
@Entity
@DiscriminatorValue("Tour")
@ManagedEntity(entityType = EntityType.Tour)
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
class Tour(

    // Transient fields, will be passed to superclass constructor
    // givenId: UUID? = null,
    tourUrl: String?,

    // Persistent fields which also become part of the superclass table
    // no longer needed: @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
    var beenThere: LocalDate? = null,

    var rating: Int = 0

) : Location(primaryUrl = tourUrl)
