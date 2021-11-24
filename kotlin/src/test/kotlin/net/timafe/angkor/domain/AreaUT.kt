package net.timafe.angkor.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AreaUT {

    @Test
    fun `it should convert country codes to their Emoji Representation`() {
        val thai = Area(code = "th",name="", parentCode = "")
        val cuba = Area(code = "cu",name="", parentCode = "")

        assertEquals("🇹🇭", thai.emoji)
        assertEquals("🇨🇺", cuba.emoji)
        assertNull(Area(code = "12",name="", parentCode = "").emoji)
    }
}
