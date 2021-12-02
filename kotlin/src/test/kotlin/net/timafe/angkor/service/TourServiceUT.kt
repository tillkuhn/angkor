package net.timafe.angkor.service

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
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.TourRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


// Wiremock setup inspired by
// https://github.com/marcinziolo/kotlin-wiremock/blob/master/src/test/kotlin/com/marcinziolo/kotlin/wiremock/AbstractTest.kt
class TourServiceUT {

    private val wireMockPort = TestHelpers.findRandomWiremockPort()
    private val wiremock: WireMockServer = WireMockServer(options().port(wireMockPort).notifier(ConsoleNotifier(true)))
    private val props: AppProperties = AppProperties()
    private val tourId = 12345678
    private val userId = 7007
    private lateinit var tourService: TourService

    @BeforeEach
    fun setUp() {
        props.tours.apiBaseUrl = "http://localhost:${wireMockPort}"
        props.tours.apiUserId = userId.toString()
        tourService = TourService(
            appProperties = props,
            repo = Mockito.mock(TourRepository::class.java),
            userService = Mockito.mock(UserService::class.java),
            eventService = Mockito.mock(EventService::class.java),
            MockServices.geoService(),
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
    fun `Test retrieve public planned tour list`() {
        testTours("planned",1)
    }

    @Test
    fun `Test retrieve public recorded tour list`() {
        testTours("recorded",2)
    }

    private fun testTours(tourType: String, records: Int) {
        // curl 'https://www.komoot.de/api/v007/users/1025061571851/tours/?sport_types=&type=tour_recorded&sort_field=date&sort_direction=desc&name=&status=public&hl=de&page=1&limit=24'

        wiremock.stubFor(
            get(urlEqualTo("/users/${userId}/tours/?type=tour_${tourType}&sort_field=date&sort_direction=desc&status=public"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("test-tours-${tourType}.json")
                )
        )
        val result = tourService.importTours(tourType)
        assertEquals(result.read, records, "Expected $tourType $records tours (read), got ${result.read}")
        /*
        body.iterator().forEach {
            assertNotNull(it.name)
            assertNotNull(it.primaryUrl)
            assertEquals(2,it.coordinates.size,"Coordinates should contain lat and lon (2 values)")
        }
         */
    }

    @Test
    fun `Test tour serialization`() {
        // See also https://www.baeldung.com/spring-boot-customize-jackson-objectmapper
        val om = ObjectMapper()
        om.registerModule(JavaTimeModule())
        om.registerModule(Jdk8Module())
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Important
        val tour = Tour("https://hase")
        tour.name = "unit test"
        val json = om.writeValueAsString(tour)
        assertTrue (json.contains(tour.name))
    }

}
