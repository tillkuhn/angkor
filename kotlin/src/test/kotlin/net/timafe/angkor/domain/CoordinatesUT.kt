package net.timafe.angkor.domain

import net.timafe.angkor.domain.dto.Coordinates
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CoordinatesUT {

    @Test
    fun `it should wrap coordinates`() {
        val co = listOf(99.1234, 11.1234)
        val coo = Coordinates(co)

        assertEquals(11.1234, coo.lat)
        assertEquals(99.1234, coo.lon)
    }

    @Test
    fun `it should handle empty list`() {
        val coo = Coordinates(listOf())

        assertEquals(null, coo.lat)
        assertEquals(null, coo.lon)
    }

    @Test
    fun `it should convert coordinates to double list`() {
        val coo = Coordinates(lon = 88.0, lat = 33.0)
        val col = coo.toList()
        assertEquals(88.0, col[0])
        assertEquals(33.0, col[1])
    }

}
