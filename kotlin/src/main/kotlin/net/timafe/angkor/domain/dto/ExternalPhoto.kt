package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class ExternalPhoto(
    val id: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String = "",
    val url: String = "",
    val description: String? = null,
    val tags: MutableList<String> = mutableListOf(),
    @JsonProperty("taken_at")
    val takenAt: ZonedDateTime = ZonedDateTime.now(),
    @JsonProperty("image_url")
    val imageUrls: List<String> = listOf(),
    @JsonProperty("shutter_speed")
    val shutterSpeed: String? = null, // "1/400"
    @JsonProperty("focal_length")
    val focalLength: String? = null, // "77"
    val aperture: String? = null, // "5.6"
)
