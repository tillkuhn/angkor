package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.service.EventService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Access all kind of events for Admin overview and entity Events
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class EventController(
    private val service: EventService
) {

    @GetMapping("/${Constants.API_PATH_ADMIN}/events")
    fun latestEvents(): List<Event> = service.findLatest()

    @GetMapping("/${Constants.API_PATH_ADMIN}/events/topic/{topic}")
    fun latestEventsByTopic(@PathVariable topic: String) = service.findLatestByTopic(topic)


}
