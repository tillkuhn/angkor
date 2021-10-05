package net.timafe.angkor.security

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ServiceAccountTokenTest {
    @Test
    fun `Test serviceAccountToken with class`() {
        val token = ServiceAccountToken(this.javaClass)
        assertEquals(this.javaClass.simpleName.lowercase(),token.name)
    }
}
