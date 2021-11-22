package net.timafe.angkor.repo

import net.timafe.angkor.domain.Photo
import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Crud Operations for Photos
 * For complex Searches, use LocationSearch
 */
interface PhotoRepository : CrudRepository<Photo, UUID>{

    fun findOneByExternalId(externalId: String): Optional<Photo>

}
