package net.timafe.angkor.domain

import net.timafe.angkor.domain.dto.Coordinates
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class CoordinatesUT {

    @Test
    fun `it should wrap coordinates`() {
        val co = listOf(99.1234, 11.1234)
        val coo = Coordinates(co)

        assertEquals(11.1234, coo.lat)
        assertEquals(99.1234, coo.lon)
    }

    // Longitude : max/min 180.0000000 to -180.0000000
    // Latitude : max/min 90.0000000 to -90.0000000
    // https://www.baeldung.com/parameterized-tests-junit-5#4-csv-literals
    @ParameterizedTest
    @CsvSource(value= ["190.0:11.0", "-190.0:11.9", "90.0:99.0","90.0:-99.0"], delimiter = ':')
    fun `it should validate ranges`(lon: Double, lat: Double) {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Coordinates(listOf(lon, lat))
        }
    }

    @Test
    fun `it should handle empty list`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Coordinates(listOf()) // not need to assign to a var since it should throw
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Coordinates(listOf(1.0)) // needs 2
        }
    }

    @Test
    fun `it should convert coordinates to double list`() {
        val coo = Coordinates(lon = 88.0, lat = 33.0)
        val col = coo.toList()
        assertEquals(88.0, col[0])
        assertEquals(33.0, col[1])
    }

}
