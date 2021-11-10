package net.timafe.angkor.domain.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class UserSummaryUT {

    @Test
    fun testUserSummary() {
        var user = UserSummary(id = UUID.randomUUID(), name = "Hase Klaus",emoji = "\uD83D\uDE48")
        Assertions.assertThat(user.shortname).isEqualTo("Hase K.")
        user = UserSummary(id = UUID.randomUUID(), name = "Horst", emoji = "\uD83D\uDE48")
        Assertions.assertThat(user.shortname).isEqualTo("Horst")
        user = UserSummary(id = UUID.randomUUID(), name = "Rudi Bacardi Sockenschorsch", emoji = "\uD83D\uDE48")
        Assertions.assertThat(user.initials).isEqualTo("RBS")
        // println(ObjectMapper().writeValueAsString(user))
    }

}
