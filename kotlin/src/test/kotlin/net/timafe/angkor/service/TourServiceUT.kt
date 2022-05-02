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
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.TourRepository
import net.timafe.angkor.web.TourController
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import kotlin.test.*


// When using this mode, a new test instance will be created once per test class.
// From Best Practices for Unit Testing in Kotlin https://phauer.com/2018/best-practices-unit-testing-kotlin/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TourServiceUT {

    private val wireMockPort = TestHelpers.findRandomWiremockPort()
    private val wiremock: WireMockServer = WireMockServer(options().port(wireMockPort).notifier(ConsoleNotifier(true)))
    private val props: AppProperties = AppProperties()
    private val tourId = 12345678
    private val userId = 7007
    private val apiVersion = "v123"
    private lateinit var tourService: TourService

    // Wiremock setup inspired by
    // https://github.com/marcinziolo/kotlin-wiremock/blob/master/src/test/kotlin/com/marcinziolo/kotlin/wiremock/AbstractTest.kt
    @BeforeEach
    fun setUp() {
        props.tours.apiBaseUrl = "http://localhost:${wireMockPort}/$apiVersion"
        props.tours.apiUserId = userId.toString()
        tourService = TourService(
            appProperties = props,
            repo = Mockito.mock(TourRepository::class.java),
            userService = Mockito.mock(UserService::class.java),
            eventService = Mockito.mock(EventService::class.java),
            MockServices.geoService(),
        )
        wiremock.start() // starts the wiremock http server
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `test tour api`() {

        wiremock.stubFor(
            get(urlEqualTo("/$apiVersion/tours/${tourId}"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        // file specified in withBodyFile should be located in directory
                        // src/test/resources/__files (cf. http://wiremock.org/docs/stubbing/)
                        .withBodyFile("test-tour-recorded.json")
                )
        )
        val body = tourService.importExternal(tourId)
        assertNotNull(body)
        assertEquals("⛩️ Some nice tour", body.name)
    }

    @Test
    fun `test tour no id`() {
        val doesNotExist = 9999
        wiremock.stubFor(
            get(urlEqualTo("/$apiVersion/tours/${doesNotExist}"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                )
        )
        val exception = assertFailsWith<ResponseStatusException> {
            tourService.importExternal(doesNotExist)
        }
        Assertions.assertThat(exception.message).contains("Could not")
    }

    @Test
    fun `Test retrieve public planned tour list`() {
        testTours("planned", 1)
    }

    @Test
    fun `Test retrieve public recorded tour list`() {
        testTours("recorded", 2)
    }

    private fun testTours(tourType: String, records: Int) {
        // curl 'https://www.tumuult.de/api/v123/users/1025061571851/tours/?sport_types=&type=tour_recorded&sort_field=date&sort_direction=desc&name=&status=public&hl=de&page=1&limit=24'

        wiremock.stubFor(
            get(urlEqualTo("/$apiVersion/users/${userId}/tours/?type=tour_${tourType}&sort_field=date&sort_direction=desc&status=public"))
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
        assertTrue(json.contains(tour.name))
    }

    @Test
    fun `Test single tour shared link url transformation public vs restricted`() {
        // HTML with shared token:
        val idPlanned = "111999111"
        val idRecorded = "999111999"
        val ir = ImportRequest(
            targetEntityType = EntityType.Tour,
            importUrl = "https://www.tumuult.de/tour/${idPlanned}?share_token=abc&ref=wtd",
        )
        wiremock.stubFor(
            get(urlEqualTo("/$apiVersion/tours/${idPlanned}?share_token=abc&ref=wtd")) // no /v123 base prefix here!
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("test-tour-planned.json")
                )
        )
        wiremock.stubFor(
            get(urlEqualTo("/$apiVersion/tours/${idRecorded}?share_token=abc&ref=wtd")) // no /v123 base prefix here!
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("test-tour-recorded.json")
                )
        )

        val ctl = TourController(tourService) // not strictly a UT, but let's also test the controller which delegates to the service
        val result = ctl.import(ir)
        assertEquals(idPlanned, result.externalId, "Expected external id $idPlanned in response")
        assertEquals(AuthScope.RESTRICTED, result.authScope, "Expected restricted authscope for private status")
        assertEquals("tour_planned", result.properties["type"], "Expected tour_planned type")
        assertEquals(2, result.coordinates.size, "Coordinates should contain lat and lon (2 values)")

        val ir2 = ImportRequest(
            targetEntityType = EntityType.Tour,
            importUrl = "https://www.tumuult.de/tour/${idRecorded}?share_token=abc&ref=wtd",
        )
        val result2 = ctl.import(ir2)
        assertEquals("tour_recorded", result2.properties["type"], "Expected tour_planned type")
        assertEquals(AuthScope.PUBLIC, result2.authScope, "Expected restricted authscope for private status")

    }

    @Test
    fun `Test hash-codes for comparison`() {
        val tour1 = Tour(tourUrl = "https://hase.de")
        tour1.name = "test1"
        tour1.externalId = "12345"
        val tour2 = Tour(tourUrl = "https://hase.de")
        tour2.name = "test1"
        tour2.externalId = "12346" // different
        val tour3 = Tour(tourUrl = "https://hase.de")
        tour3.name = "test1"
        tour3.externalId = "12345" // different
        tour3.areaCode = "de"
        // first  code is 28995533
        assertNotEquals(tourService.keyFieldsHashCode(tour1),tourService.keyFieldsHashCode(tour2))
        assertEquals(tourService.keyFieldsHashCode(tour1),tourService.keyFieldsHashCode(tour3))
    }


}
