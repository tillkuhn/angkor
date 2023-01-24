package net.timafe.angkor.service

import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.PostRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class PostServiceUT {

    @Test
    fun `test article import from wp backup on local filesystem`() {
        // System.getProperty("user.home")+"/.angkor/import"
        // see comment in PhotoServiceUT for why we need this hack
        val repo = Mockito.mock(PostRepository::class.java)
        Mockito.`when`(repo.save(TestHelpers.any())).thenAnswer{ i -> i.arguments[0]}

        val resourceDirectory: Path = Paths.get("src", "test", "resources", "import")
        val service = PostService(
            repo,
            resourceDirectory.absolutePathString(),
            MockServices.geoService(),
            MockServices.userService(),
        )
        service.import()
    }
}
