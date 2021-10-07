package net.timafe.angkor.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.Tour
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * JSON ObjectMapper tests
 *
 * See also: https://www.baeldung.com/jackson-object-mapper-tutorial
 */
class ObjectMapperUT {

    private lateinit var om: ObjectMapper

    @BeforeEach
    fun setUp() {
        om = JacksonConfig().objectMapper()
    }

    @Test
    fun `Given a json string with missing and unknown fields, it should still deserialize to place`() {
        val json = """{"name":"test place", "areaCode":"de", "unknownField":"hello","lastVisited":"2021-12-31" }"""
        val p = om.readValue(json,Place::class.java)
        assertEquals("test place",p.name)
        assertEquals("de",p.areaCode)
        // Ensure kotlin module is registered, or those value would be null (even though they are declared not null)
        assertNotNull(p.tags)
        assertNotNull(p.coordinates)
        // Make sure date can be parsed to java.time.LocalDate
        assertEquals(2021,p.lastVisited?.year)
    }

    @Test
    fun `Given a json string without id, tour object should be initialized with a random uuid`() {
        val json = """{"name":"test place","externalId":"12345"}"""
        val t = om.readValue(json,Tour::class.java)
        assertNotNull(t.id)
        assertEquals(0,t.rating) // also make sure this doesn't fail even if property is not present
    }

}
