package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.repo.EventRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.core.env.Environment
import java.util.*

class EventServiceUT {

    /**
     * An example how to unit test private methods
     * based on https://medium.com/mindorks/how-to-unit-test-private-methods-in-java-and-kotlin-d3cae49dccd
     */
    @Test
    fun testDigest() {
        val appProperties = AppProperties()
        val eventService = EventService(
            repo = Mockito.mock(EventRepository::class.java),
            objectMapper = ObjectMapper(),
            appProps = appProperties,
            env = Mockito.mock(Environment::class.java),
            userService = Mockito.mock(UserService::class.java),
        )
        eventService.init()
        val event = Event(action = "create:place", message = "Hello", entityId = UUID.fromString(Constants.USER_SYSTEM))
        val method = eventService.javaClass.getDeclaredMethod("recommendKey", Event::class.java)
        method.isAccessible = true
        val outcome = method.invoke(eventService, event) //
        Assertions.assertThat(outcome).isEqualTo("2081359542")

    }
}
