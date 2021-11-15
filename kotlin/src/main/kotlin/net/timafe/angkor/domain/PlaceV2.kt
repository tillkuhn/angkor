package net.timafe.angkor.domain

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("PLACE")
class PlaceV2(

    @Column
    var summary: String? = null

) : Location()
