package net.timafe.angkor.service

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
        val service = PostService(Mockito.mock(PostRepository::class.java),resourceDirectory.absolutePathString())
        service.import()
    }
}
