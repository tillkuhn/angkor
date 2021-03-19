package net.timafe.angkor.service

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [Event]
 */
@Service
@Transactional
class EventService(
    private val repo: EventRepository
) : EntityService<Event, Event, UUID>(repo) {

    override fun entityType(): EntityType {
        return EntityType.EVENT
    }

}
