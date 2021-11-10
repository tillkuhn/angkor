package net.timafe.angkor.domain.dto

/**
 * Think wrapper over List<Double> for Latitude and Longitude
 *
 */
data class Coordinates(
    val lon: Double?,
    val lat: Double?,
) {
    constructor(coordinateList: List<Double>) : this(coordinateList.getOrNull(0), coordinateList.getOrNull(1))

    fun toList(): List<Double> {
        return if (lon != null && lat != null) listOf(lon,lat) else listOf()
    }
}
