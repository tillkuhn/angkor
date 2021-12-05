package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class ExternalPhoto(
    val id: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String? = null,
    val description: String? = null,
    val tags: List<String> = listOf(),
    @JsonProperty("taken_at")
    val takenAt: ZonedDateTime = ZonedDateTime.now(),

    )
