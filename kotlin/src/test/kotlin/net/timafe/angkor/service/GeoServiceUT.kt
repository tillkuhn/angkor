package net.timafe.angkor.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import net.timafe.angkor.domain.dto.Coordinates
import net.timafe.angkor.helper.TestHelpers
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeoServiceUT {

    private val wireMockPort = TestHelpers.findRandomWiremockPort()
    private val wiremock: WireMockServer = WireMockServer(
        WireMockConfiguration
            .options()
            .port(wireMockPort)
            .notifier(ConsoleNotifier(true))
    )
    private lateinit var geoService: GeoService

    @BeforeEach
    fun setUp() {
        val osmApiServiceUrl = "http://localhost:${wireMockPort}"
        geoService = GeoService(osmApiServiceUrl)
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `it should locate Wat Arun in Bangkok Thailand with reverse lookup`() {
        // expect  /reverse?lat=13.743913&lon=100.488488&format=jsonv2
        val coordinates = Coordinates(100.488488, 13.743913)
        wiremock.stubFor(
            WireMock.get(WireMock.urlEqualTo("/reverse?lat=${coordinates.lat}&lon=${coordinates.lon}&format=jsonv2"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        // file specified in withBodyFile should be in src/test/resources/__files.
                        // Read more: http://wiremock.org/docs/stubbing/
                        .withBodyFile("test-reverse-geocoding.json")
                )
        )
        val resp = geoService.reverseLookup(coordinates)
        assertNotNull(resp)
        assertEquals(23481741, resp.osmId)
        // assertEquals(106523360,resp.placeId) // place id is not deterministic (different in CI) - refactor!
        assertEquals("th", resp.countryCode)
        assertTrue(resp.name!!.isNotEmpty())
        assertTrue(resp.type!!.isNotEmpty())
        assertEquals("place_of_worship",resp.type)
        Assertions.assertThat(resp.lat).isGreaterThan(0.0)
        Assertions.assertThat(resp.lon).isGreaterThan(0.0)
    }
}
