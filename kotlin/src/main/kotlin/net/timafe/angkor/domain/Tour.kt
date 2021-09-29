package net.timafe.angkor.domain

import org.springframework.data.repository.CrudRepository
import java.util.*
import javax.persistence.Column

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("TOUR")
class Tour(
    id: UUID = UUID.randomUUID(),

    @Column
    var summary: String? = null
) : Location(id)

interface TourRepository : CrudRepository<Tour, UUID>
