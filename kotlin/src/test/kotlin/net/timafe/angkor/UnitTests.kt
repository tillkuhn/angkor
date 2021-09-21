package net.timafe.angkor

import net.timafe.angkor.config.SecurityConfig
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LinkMediaType
import net.timafe.angkor.service.LinkService
import net.timafe.angkor.service.TaggingService
import net.timafe.angkor.web.LinkController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import java.util.*
import kotlin.test.assertEquals

/**
 * Fast Unit tests to be run in isolation - no spring container required
 */
class UnitTests {

    private val taggingService = TaggingService()

    @Test
    fun testEnum() {
        assertEquals("ALL_AUTH", AuthScope.ALL_AUTH.name)
        assertEquals("All auth", AuthScope.ALL_AUTH.friendlyName())
        assertEquals("Public", AuthScope.PUBLIC.friendlyName())
    }

    // https://www.baeldung.com/parameterized-tests-junit-5#4-csv-literals
    @ParameterizedTest
    @CsvSource(value= ["Sri Lanka North:sri-lanka-north", "Hase:hase"], delimiter = ':')
    fun testTags(code: String, expected: String) {
        assertThat(taggingService.normalizeTag(code)).isEqualTo(expected)
    }

    @Test
    fun testUserSummary() {
        var user = UserSummary(id = UUID.randomUUID(), name = "Hase Klaus",emoji = "\uD83D\uDE48")
        assertThat(user.shortname).isEqualTo("Hase K.")
        user = UserSummary(id = UUID.randomUUID(), name = "Horst", emoji = "\uD83D\uDE48")
        assertThat(user.shortname).isEqualTo("Horst")
        user = UserSummary(id = UUID.randomUUID(), name = "Rudi Bacardi Sockenschorsch", emoji = "\uD83D\uDE48")
        assertThat(user.initials).isEqualTo("RBS")
        // println(ObjectMapper().writeValueAsString(user))
    }

    @Test
    fun testSecurityConfig() {
        val sc = SecurityConfig()
        val patterns = sc.getEntityPatterns("/hase")
        assertThat(patterns.size).isEqualTo(EntityType.values().size)
    }

    @Test
    fun testLinkMediaTypes() {
        val types = LinkController(Mockito.mock(LinkService::class.java)).getLinkMediaTypes()
        assertThat(types.size).isEqualTo(LinkMediaType.values().size)
        types.forEach{ assertThat(it.icon).isNotBlank()  }
        types.forEach{ assertThat(it.label).isNotBlank()  }
    }

}
