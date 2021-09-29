package net.timafe.angkor.domain

import org.springframework.data.repository.CrudRepository
import java.util.*

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("TOUR")
class Tour(
    id: UUID = UUID.randomUUID(),
    tourUrl: String?,
    var summary: String? = null
) : Location(id = id, primaryUrl = tourUrl)

interface TourRepository : CrudRepository<Tour, UUID>
