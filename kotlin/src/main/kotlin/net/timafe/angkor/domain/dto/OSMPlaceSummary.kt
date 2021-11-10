package net.timafe.angkor.domain.dto

data class OSMPlaceSummary(
    val osmId: Long,
    val placeId: Long,
    val type: String?,
    val lat: Double,
    val lon: Double,
    val countryCode: String,
    val name: String?,
)
