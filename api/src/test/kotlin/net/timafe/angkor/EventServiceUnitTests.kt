package net.timafe.angkor

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.dto.EventMessage
import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.repo.EventRepository
import net.timafe.angkor.service.EventService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.core.env.Environment

class EventServiceUnitTests {

    @Test
    fun testDigest() {
        val appProperties = AppProperties()
        val eventService = EventService(
            Mockito.mock(EventRepository::class.java),
            ObjectMapper(),
            appProperties,
            Mockito.mock(Environment::class.java)
        )
        eventService.init()
        val event = EventMessage(action = "create:place", message = "huhu", entityId = "1234")
        Assertions.assertThat(eventService.eventKey(event)).isEqualTo("1509442")

    }
}
