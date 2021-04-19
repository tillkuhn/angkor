package net.timafe.angkor.repo

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.enums.LinkMediaType
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LinkRepository : CrudRepository<Link, UUID> {

    companion object {
        const val FEED_CACHE = "feedCache"
    }

    override fun findAll(): List<Link>

    @Query("SELECT COUNT(l) FROM Link l where l.mediaType = net.timafe.angkor.domain.enums.LinkMediaType.VIDEO")
    fun videoCount(): Long

    /* Feeds */
    @Cacheable(FEED_CACHE)
    @Query("SELECT l FROM Link l where l.mediaType = net.timafe.angkor.domain.enums.LinkMediaType.FEED ORDER BY l.createdAt")
    fun findAllFeeds(): List<Link>

    @Query("SELECT COUNT(l) FROM Link l where l.mediaType = net.timafe.angkor.domain.enums.LinkMediaType.FEED")
    fun feedCount(): Long

    // todo add index on name, append OrderByName
    fun findByMediaType(mediaType: LinkMediaType): List<Link>

}
