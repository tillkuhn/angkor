package net.timafe.angkor.domain.enums

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthScopeUT {

    @Test
    fun testEnum() {
        assertEquals("ALL_AUTH", AuthScope.ALL_AUTH.name)
        assertEquals("All auth", AuthScope.ALL_AUTH.friendlyName())
        assertEquals("Public", AuthScope.PUBLIC.friendlyName())
    }

}
