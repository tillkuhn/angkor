package net.timafe.angkor.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.ImportRequest
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.VideoRepository
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.util.*

/**
 * Rest Bridge to external provider for Video Information
 */
@Service
class VideoService(
    private val repo: VideoRepository, // must use "private val ..." to access it in this class
    geoService: GeoService,
    @Value("\${app.videos.api-base-url}")  private val apiBaseUrl: String,
):  AbstractLocationService<Video, Video, UUID>(repo, geoService)  {

    override fun entityType(): EntityType = EntityType.Video

    /**
     * Map oembed json structure to internal video
     */
    private fun mapJSONObject(jsonVideo: JSONObject): Video {
        val  video = Video()
        video.apply {
            name = jsonVideo.getString("title")
            imageUrl = jsonVideo.getString("thumbnail_url")
            authScope = AuthScope.ALL_AUTH
            properties["author_name"] = jsonVideo.getString("author_name")
        }
        video.tags.add("video")
        return video
    }

    /**
     * Use oembed interface to download video information for the external URL
     * essentially we have to call ...
     * https://www.senftube.com/oembed?url=<url-encoded-original-url>&format=json
     * --- and evaluate the response
     */
    fun importExternal(importRequest: ImportRequest): Video {
        val targetEncUrl = URLEncoder.encode(importRequest.importUrl,"UTF-8")
        val url = "${apiBaseUrl}/oembed?url=${targetEncUrl}&format=json"
        val jsonResponse: HttpResponse<JsonNode> = Unirest.get(url)
            .header("accept", "application/json")
            .asJson()
        // extract tour id later
        log.info("${logPrefix()} Downloading tour video for ${importRequest.importUrl} from $url status=${jsonResponse.status}")
        if (jsonResponse.status != HttpStatus.OK.value()) {
            throw ResponseStatusException(
                HttpStatus.valueOf(jsonResponse.status),
                "Could not retrieve tour info from $url"
            )
        }
        val jsonObject = jsonResponse.body.`object`
        val video = mapJSONObject(jsonObject)
        video.primaryUrl = importRequest.importUrl
        val externalId = extractId(importRequest.importUrl)
        video.externalId = externalId

        // persist i
        val existVideo = repo.findOneByExternalId(externalId)
        if (existVideo.isEmpty) {
            log.info("${logPrefix()} Importing new video ${video.name}")
            this.save(video)
        } else {
            log.debug("{} {} already stored {} ", logPrefix(), video.name, existVideo.get().id)
        }
        return video
    }

    // https://senftu.be/8PGQJN5S20w
    // https://www.senftube.com/watch?v=LtfS5hgkvt8
    private fun extractId(url: String):String {
        return if ( url.contains("/watch?v=")) {
            url.substringAfterLast("=")
        } else {
            url.substringAfterLast("/")
        }
    }
}
