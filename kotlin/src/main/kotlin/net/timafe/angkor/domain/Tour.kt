package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import java.time.LocalDate
import java.util.*
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("TOUR")
class Tour(

    // Transient fields, will be passed to superclass constructor
    // givenId: UUID? = null,
    tourUrl: String?,

    // Persistent fields
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
    var beenThere: LocalDate? = null,

) : Location( /*givenId = givenId,*/ primaryUrl = tourUrl)
