package net.timafe.angkor.service

import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.LinkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [Link].
 */
@Service
@Transactional
class LinkService(
    private val repo: LinkRepository
) : EntityService<Link, Link, UUID>(repo) {

    @Transactional(readOnly = true)
    fun findAllVideos(): List<Link> = repo.findAllVideos()

    @Transactional(readOnly = true)
    fun findAllFeeds(): List<Link> = repo.findAllFeeds()

    override fun entityType(): EntityType {
        return EntityType.LINK
    }

}
