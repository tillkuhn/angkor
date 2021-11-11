package net.timafe.angkor.domain.dto

/**
 * Value holder for info returned from OSM reverse geocoding API
 */
data class GeoPoint(
    val osmId: Long,
    val lat: Double,
    val lon: Double,
    val countryCode: String,
    val type: String?,
    val name: String?,
)
