package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.GeoPoint
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.PostRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class PostServiceUT {

    @Test
    fun `test article import from wp backup`() {
        // System.getProperty("user.home")+"/.angkor/import"
        val resourceDirectory: Path = Paths.get("src", "test", "resources", "import")
        val geoPoint = GeoPoint(123,1.0,2.0,"th","temple","Thailand")
        val geoService = Mockito.mock(GeoService::class.java)
        Mockito.`when`(geoService.reverseLookup(TestHelpers.mockitoAny())).thenReturn(geoPoint)
        Mockito.`when`(geoService.reverseLookupWithRateLimit(TestHelpers.mockitoAny())).thenReturn(geoPoint)
        val service = PostService(
            Mockito.mock(PostRepository::class.java),
            resourceDirectory.absolutePathString(),
            geoService
        )
        service.import()
    }
}
