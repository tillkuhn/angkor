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

    /**
     * An example how to unit test private methods
     * based on https://medium.com/mindorks/how-to-unit-test-private-methods-in-java-and-kotlin-d3cae49dccd
     */
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
        val method = eventService.javaClass.getDeclaredMethod("eventKey", EventMessage::class.java)
        method.isAccessible = true
        val outcome = method.invoke(eventService, event) //
        Assertions.assertThat(outcome).isEqualTo("33030347")

    }
}
