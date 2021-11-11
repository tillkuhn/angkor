package net.timafe.angkor.domain.dto

/**
 * Think wrapper over List<Double> for Latitude and Longitude
 *
 * Todo validate range https://stackoverflow.com/a/23914607/4292075
 * - Latitude : max/min 90.0000000 to -90.0000000
 * - Longitude : max/min 180.0000000 to -180.0000000
 */
// https://www.programiz.com/kotlin-programming/constructors
//
// The block of code surrounded by parentheses is the primary constructor:
// The primary constructor has a constrained syntax, and cannot contain any code.
// To put the initialization code (not only code to initialize properties), initializer block is used.
// It is prefixed with init
data class Coordinates(
    val lon: Double,
    val lat: Double,
) {
    constructor(coordinateList: List<Double>) : this(
        // Can we throw here in the secondary constructor? Yes we can!
        // https://discuss.kotlinlang.org/t/should-secondary-constructor-throw/12282/6
        if (coordinateList.size < 2) {
            throw IllegalArgumentException("coordinateList must have at least 2 elements")
        } else coordinateList[0], coordinateList[1]
    )

    // Validate lat/lon ranges
    init {
        if (lat < -90.0 || lat > 90.0) {
            throw IllegalArgumentException("Invalid range for lat (90.0000000 to -90.0000000)")
        }
        if (lon < -180.0 || lon > 180.0) {
            throw IllegalArgumentException("Invalid range for lon (180.0000000 to -180.0000000)")
        }
    }

    fun toList(): List<Double> {
        return listOf(lon, lat)
    }
}
