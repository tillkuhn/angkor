package net.timafe.angkor.service

import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.VideoRepository
import net.timafe.angkor.security.SecurityUtils
import org.springframework.stereotype.Service
import java.util.*

/**
 * Rest Bridge to external provider for Video Information
 */
@Service
class VideoService(
    private val repo: VideoRepository,
): AbstractEntityService<Video, Video, UUID>(repo)  {


    /**
     * The better findAll method (which uses JPA Query with implicit filter on authscope
     */
    override fun findAll(): List<Video> {
        val authScopes = SecurityUtils.allowedAuthScopes()
        val items = this.repo.findAllByAuthScope(authScopes)
        this.log.info("${logPrefix()} FindAll: ${items.size} results, authScopes $authScopes")
        return items
    }

    override fun entityType(): EntityType = EntityType.VIDEO

}
