package net.timafe.angkor.service

import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.VideoRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * Rest Bridge to external provider for Video Information
 */
@Service
class VideoService(
    private val repo: VideoRepository,
    geoService: GeoService,
):  AbstractLocationService<Video, Video, UUID>(repo, geoService)  {

    override fun entityType(): EntityType = EntityType.Video

}
