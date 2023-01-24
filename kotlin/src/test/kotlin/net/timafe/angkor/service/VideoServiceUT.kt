package net.timafe.angkor.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.VideoRepository
import net.timafe.angkor.web.VideoController
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


// When using this mode, a new test instance will be created once per test class.
// https://phauer.com/2018/best-practices-unit-testing-kotlin/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoServiceUT {

    private val wireMockPort = TestHelpers.findRandomWiremockPort()
    private val wiremock: WireMockServer = WireMockServer(options().port(wireMockPort).notifier(ConsoleNotifier(true)))
    private val props: AppProperties = AppProperties()
    private lateinit var videoService: VideoService

    // Wiremock setup inspired by
    // https://github.com/marcinziolo/kotlin-wiremock/blob/master/src/test/kotlin/com/marcinziolo/kotlin/wiremock/AbstractTest.kt
    @BeforeEach
    fun setUp() {
        props.tours.apiBaseUrl = "http://localhost:${wireMockPort}"
        val repo = Mockito.mock(VideoRepository::class.java)
        // see comment in PhotoServiceUT for why we need this hack
        Mockito.`when`(repo.save(TestHelpers.any())).thenAnswer{ i -> i.arguments[0]}
        videoService = VideoService(
            repo,
            MockServices.geoService(),
             "http://localhost:${wireMockPort}"
        )
        wiremock.start() // starts the wiremock http server
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }
    
    @Test
    fun `Test single video with oembed retrieval`() {
        val testId = "AA999111333bb"
        val ir = ImportRequest(
            targetEntityType = EntityType.Video,
            importUrl = "https://senftu.be/${testId}",
            // urlencode https%3A%2F%2Fsenftu.be%2FAA999111333bb
        )
        wiremock.stubFor(
            get(urlEqualTo("/oembed?url=https%3A%2F%2Fsenftu.be%2F${testId}&format=json"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("test-video-oembed.json")
                )
        )
        val ctl = VideoController(videoService)
        val video = ctl.import(ir)
        assertNotNull(video)
        assertEquals("Flight of the Dragon @ Fairytale",video.name)
        assertEquals(testId,video.externalId)
        assertEquals("https://i.senftube-thumb.com/vi/${testId}/hqdefault.jpg",video.imageUrl)
        assertEquals("Testa",video.properties["author_name"])


    }


}
