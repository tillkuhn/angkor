package net.timafe.angkor.repo

import net.timafe.angkor.domain.Post
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Crud Operations for Videos
 * For complex Searches, use LocationSearch
 */
interface PostRepository : CrudRepository<Post, UUID>{

    fun findOneByExternalId(externalId: String): Optional<Post>

    // todo should be handled centrally together with video, tour ...
    @Query("SELECT COUNT(p) FROM Post p")
    fun itemCount(): Long

}
