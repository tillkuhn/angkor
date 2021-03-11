package net.timafe.angkor.repo

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Link
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LinkRepository : CrudRepository<Link, UUID> {

    override fun findAll(): List<Link>

    @Query("SELECT l FROM Link l where l.mediaType = net.timafe.angkor.domain.enums.MediaType.VIDEO ORDER BY l.name")
    fun findAllVideos(): List<Link>
}
