package net.timafe.angkor.domain

import java.util.*
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("TOUR")
class Tour(
    givenId: UUID? =null,
    tourUrl: String?,
    var summary: String? = null
) : Location(givenId = givenId, primaryUrl = tourUrl)
