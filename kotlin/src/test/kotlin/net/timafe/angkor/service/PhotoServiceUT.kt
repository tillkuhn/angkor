package net.timafe.angkor.service

import net.timafe.angkor.repo.PhotoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals

// https://phauer.com/2018/best-practices-unit-testing-kotlin/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhotoServiceUT {

    private val repo = Mockito.mock(PhotoRepository::class.java)
    private val feedPath: Path = Paths.get("src", "test", "resources", "import","test-999px-feed.xml")

    @Test
    fun `it should import external photos form json`() {
        val resourceDirectory: Path = Paths.get("src", "test", "resources", "import")
        val service = PhotoService(
            feedPath.toString(), repo,
            MockServices.geoService(), MockServices.areaService(),
            MockServices.userService(),
            MockServices.objectMapper(),
        )
        val result = service.importFromFolder(resourceDirectory.toString())
        assertEquals(1,result.read)
    }

    @Test
    fun `test extract external photos from RSS feed`() {
        // service.parseFeed("https://500px.com/tillkuhn/rss")

        val service = PhotoService(feedPath.toString(),repo,
            MockServices.geoService(),MockServices.areaService(),
            MockServices.userService(),
            MockServices.objectMapper(),
        )
        service.import()

        // service = PhotoService("https://500px.com/somebody/rss",repo, MockServices.geoService())
        // service.import()

    }
}
