package net.timafe.angkor.domain.dto

data class ExternalTour(
    val name: String,
    val externalId: Int,
    val coordinates: List<Double> = listOf() /* lon,lat*/,
    val altitude: Int
)
