package net.timafe.angkor.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.dto.Coordinates
import net.timafe.angkor.service.MockServices
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * JSON ObjectMapper tests
 *
 * See also: https://www.baeldung.com/jackson-object-mapper-tutorial
 */
class JacksonConfigUT {

    private lateinit var om: ObjectMapper

    @BeforeEach
    fun setUp() {
        om = JacksonConfig().objectMapper()
    }

    @Test
    fun `Given an object, Jackson 3 JsonMapper Builder should return indent output with test profile and no pretty print in prod`() {
        val dto = Coordinates(lon = 12.34, lat = 56.78)
        val jmbTest = JsonMapper.builder()
        JacksonConfig().customizer(MockServices.environment("test")).customize(jmbTest)
        jmbTest.build().writeValueAsString(dto).also {
            // println("Serialized JSON with test profile:\n$it")
            // Assert pretty print (indentation)
            assert(it.contains("\n  ")) // PROD: {"lon":12.34,"lat":56.78} (no pretty print)
        }
        JacksonConfig().customizer(MockServices.environment("prod")).customize(jmbTest)
        jmbTest.build().writeValueAsString(dto).also {
            // Assert no pretty print (indentation) in prod: {"lon":12.34,"lat":56.78}
            assertFalse( it.contains("\n"))
            assertEquals("""{"lon":12.34,"lat":56.78}""",it,"Message alphabetical order is disabled, so lon comes before lat as it is declared first" )
        }
    }

    @Test
    fun `Given a json string with missing and unknown fields, it should still deserialize to place`() {
        val json = """{"name":"test place", "areaCode":"de", "unknownField":"hello","beenThere":"2021-12-31" }"""
        val p = om.readValue(json,Place::class.java)
        assertEquals("test place",p.name)
        assertEquals("de",p.areaCode)
        // Ensure kotlin module is registered, or those value would be null (even though they are declared not null)
        assertNotNull(p.tags)
        assertNotNull(p.coordinates)
        // Make sure date can be parsed to java.time.LocalDate
        assertEquals(2021,p.beenThere?.year)
    }

    @Test
    fun `Given a json string without id, tour object should be initialized with a random uuid`() {
        val json = """{"name":"test place","externalId":"12345"}"""
        val t = om.readValue(json,Tour::class.java)
        assertNotNull(t.id)
        assertEquals(0,t.rating) // also make sure this doesn't fail even if property is not present
    }

}
