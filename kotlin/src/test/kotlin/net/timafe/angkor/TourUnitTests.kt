package net.timafe.angkor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.repo.TourRepository
import net.timafe.angkor.service.EventService
import net.timafe.angkor.service.TaggingService
import net.timafe.angkor.service.TourService
import net.timafe.angkor.service.UserService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import java.net.ServerSocket
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


// Wiremock setup inspired by
// https://github.com/marcinziolo/kotlin-wiremock/blob/master/src/test/kotlin/com/marcinziolo/kotlin/wiremock/AbstractTest.kt
class TourUnitTests {

    private val wireMockPort = findRandomPort()
    private val wiremock: WireMockServer = WireMockServer(options().port(wireMockPort).notifier(ConsoleNotifier(true)))
    private val props: AppProperties = AppProperties()
    private val tourId = 12345678
    private val userId = 7007
    private lateinit var tourService: TourService

    @BeforeEach
    fun setUp() {
        props.tourApiBaseUrl = "http://localhost:${wireMockPort}"
        props.tourApiUserId = userId.toString()
        tourService = TourService(
            appProperties = props,
            tourRepository = Mockito.mock(TourRepository::class.java),
            userService = Mockito.mock(UserService::class.java),
            eventService = Mockito.mock(EventService::class.java),
        )
     wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `test tour api`() {

        wiremock.stubFor(
            get(urlEqualTo("/tours/${tourId}"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        // file specified in withBodyFile should be in src/test/resources/__files.
                        // Read more: http://wiremock.org/docs/stubbing/
                        .withBodyFile("test-tour.json")
                )
        )
        val body = tourService.loadSingleExternalTour(tourId)
        assertNotNull(body)
        assertEquals("⛩️ Some nice tour", body.name)
    }

    @Test
    fun `test tour no id`() {
        val doesNotExist = 9999
        wiremock.stubFor(
            get(urlEqualTo("/tours/${doesNotExist}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                )
        )
        val exception = assertFailsWith<ResponseStatusException> {
            tourService.loadSingleExternalTour(doesNotExist)
        }
        Assertions.assertThat(exception.message).contains("Could not")
    }

    @Test
    fun `Test retrieve public tour list`() {
        // curl 'https://www.komoot.de/api/v007/users/1025061571851/tours/?sport_types=&type=tour_recorded&sort_field=date&sort_direction=desc&name=&status=public&hl=de&page=1&limit=24'

        wiremock.stubFor(
            get(urlEqualTo("/users/${userId}/tours/?type=tour_recorded&sort_field=date&sort_direction=desc&status=public"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("test-tours.json")
                )
        )
        val body = tourService.loadTourList()
        assertTrue(body.size == 2, "Expected 2 tours, got ${body.size}")
        body.iterator().forEach {
            assertNotNull(it.name)
            assertNotNull(it.primaryUrl)
            assertEquals(2,it.coordinates.size)
        }
    }

    @Test
    fun `Test tour serilization`() {
        // See also https://www.baeldung.com/spring-boot-customize-jackson-objectmapper
        val om = ObjectMapper()
        om.registerModule(JavaTimeModule())
        om.registerModule(Jdk8Module())
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Important
        val tour = Tour("https://hase")
        tour.name = "unit test"
        val json = om.writeValueAsString(tour)
        assertTrue (json.contains(tour.name))
    }

    // https://dzone.com/articles/kotlin-wiremock
    private fun findRandomPort(): Int {
        ServerSocket(0).use { socket -> return socket.localPort }
    }

}
