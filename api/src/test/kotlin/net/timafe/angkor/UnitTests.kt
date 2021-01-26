package net.timafe.angkor

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.domain.enums.AreaLevel
import net.timafe.angkor.domain.enums.AuthScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class UnitTests {

    @Test
    fun testEnum() {
        assertEquals(AuthScope.ALL_AUTH.name,"ALL_AUTH")
    }

    @Test
    fun testUserSummary() {
        var user = UserSummary(id = UUID.randomUUID(), name = "Hase Klaus")
        assertThat(user.shortname).isEqualTo("Hase K.")
        user = UserSummary(id = UUID.randomUUID(), name = "Horst")
        assertThat(user.shortname).isEqualTo("Horst")
        // println(ObjectMapper().writeValueAsString(user))
    }


}
