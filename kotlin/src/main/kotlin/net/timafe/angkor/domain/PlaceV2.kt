package net.timafe.angkor.domain

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


/**
 * Entity that represents a Place
 * with some an external url reference
 */
@Entity
@DiscriminatorValue("Place")
class PlaceV2(

    @Column
    var summary: String? = null

) : Location()
