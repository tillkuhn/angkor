package net.timafe.angkor.repo

import net.timafe.angkor.domain.Event
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EventRepository : CrudRepository<Event, UUID> {

    override fun findAll(): List<Event>
}
