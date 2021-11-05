package net.timafe.angkor.repo

import net.timafe.angkor.domain.Post
import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Crud Operations for Videos
 * For complex Searches, use LocationSearch
 */
interface PostRepository : CrudRepository<Post, UUID>{

    fun findOneByExternalId(externalId: String): Optional<Post>
}
