package net.timafe.angkor.repo

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.POI
import net.timafe.angkor.domain.dto.PlaceSummary
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface EventRepository : CrudRepository<Event, UUID> {

    override fun findAll(): List<Event>
}
