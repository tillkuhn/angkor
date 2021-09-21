package net.timafe.angkor

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.service.TourService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.net.ServerSocket
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

// Wiremock setup inspired by
// https://github.com/marcinziolo/kotlin-wiremock/blob/master/src/test/kotlin/com/marcinziolo/kotlin/wiremock/AbstractTest.kt
class TestTourUnitTests {

    private val wireMockPort = findRandomPort()
    val wiremock: WireMockServer
    val props: AppProperties

    init {
        wiremock = WireMockServer(options().port(wireMockPort).notifier(ConsoleNotifier(true)))
        props = AppProperties()
        props.tourApiBaseUrl = "http://localhost:${wireMockPort}"
    }


    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `test tour api`() {
        val id = 12345678
        wiremock.stubFor(get(urlEqualTo("/tours/${id}"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/hal+json;charset=utf-8")
                .withStatus(200)
                //  file specified in withBodyFile should be in src/test/resources/__files.
                 .withBodyFile("test-tour.json")
                ))
        val ts = TourService(props)
        val body = ts.loadExternal(id)
        assertNotNull(body);
        assertEquals("⛩️ Some nice tour",body.name)


    }

    @Test
    fun `test tour no id`() {
        val notexists = 9999
        wiremock.stubFor(get(urlEqualTo("/tours/${notexists}"))
            .willReturn(aResponse()
                .withStatus(404)))
        val ts = TourService(props)
        val exception = assertFailsWith<ResponseStatusException> {
            ts.loadExternal(notexists)
        }
        Assertions.assertThat(exception.message).contains("Could not")
    }

        // https://dzone.com/articles/kotlin-wiremock
    fun findRandomPort(): Int {
        ServerSocket(0).use { socket -> return socket.localPort }
    }

}
