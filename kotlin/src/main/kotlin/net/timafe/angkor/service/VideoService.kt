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

    override fun entityType(): EntityType = EntityType.VIDEO

}
