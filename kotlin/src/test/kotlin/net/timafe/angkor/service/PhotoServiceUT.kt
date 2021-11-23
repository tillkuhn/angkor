package net.timafe.angkor.service

import net.timafe.angkor.repo.PhotoRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths

class PhotoServiceUT {

    @Test
    fun `test extract photos from rss feed`() {
        // service.parseFeed("https://500px.com/tillkuhn/rss")
        val feedPath: Path = Paths.get("src", "test", "resources", "import","test-999px-feed.xml")
        val repo = Mockito.mock(PhotoRepository::class.java)

        var service = PhotoService(feedPath.toString(),repo, MockServices.geoService(),MockServices.areaService())
        service.import()

        // service = PhotoService("https://500px.com/somebody/rss",repo, MockServices.geoService())
        // service.import()

    }
}
